package com.vvq.query.jpa.builder;

import com.vvq.query.jpa.builder.column.ColumnQuery;
import com.vvq.query.jpa.builder.context.QuerySupplierContext;
import com.vvq.query.jpa.builder.helper.RepositoryHelper;
import com.vvq.query.jpa.builder.supplier.AfterTuplePopulatedSupplier;
import com.vvq.query.jpa.builder.supplier.OrderBySupplier;
import com.vvq.query.jpa.builder.supplier.PredicatesSupplier;
import com.vvq.query.jpa.builder.supplier.SelectionsSupplier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

@Getter
@SuperBuilder
public abstract class FromQuerySelections {

  OrderBySupplier orderBySupplier;
  SelectionsSupplier selectionsSupplier;
  PredicatesSupplier predicatesSupplier;
  AfterTuplePopulatedSupplier afterTuplePopulated;

  List<Selection<?>> multiSelections;
  Map<String, Join<? extends QueryBuilderPersistable, ? extends QueryBuilderPersistable>> joins;
  List<Root<?>> additionalRoots;

  public abstract List<ColumnQuery.ColumnQueryBuilder> getColumnQueries();

  public List<Selection<?>> getMultiSelections() {
    return multiSelections == null ? Collections.emptyList() : multiSelections;
  }

  public void addMultiSelections(Root<?> root, CriteriaBuilder cb) {
    if (this.selectionsSupplier != null) {
      if (this.multiSelections == null) {
        this.multiSelections = new ArrayList<>(10);
      }
      this.multiSelections.addAll(
          this.selectionsSupplier.getSelections(createQuerySupplierContext(root), cb));
    }
  }

  public void addJoin(
      String name,
      Join<? extends QueryBuilderPersistable, ? extends QueryBuilderPersistable> join) {
    if (this.joins == null) {
      this.joins = new ConcurrentHashMap<>(5);
    }
    if (!this.joins.containsKey(name)) {
      this.joins.put(name, join);
    }
  }

  public void addJoins(
      Map<String, Join<? extends QueryBuilderPersistable, ? extends QueryBuilderPersistable>>
          joins) {
    if (this.joins == null) {
      this.joins = new ConcurrentHashMap<>(5);
    }
    this.joins.putAll(joins);
  }

  public void addAdditionalRoots(List<Root<?>> anotherRoots) {
    if (this.additionalRoots == null) {
      this.additionalRoots = new ArrayList<>(5);
    }
    this.additionalRoots.addAll(anotherRoots);
  }

  public List<Order> orderBy(Root<?> root, CriteriaBuilder cb) {
    if (this.orderBySupplier != null) {
      return this.orderBySupplier.getOrderBy(createQuerySupplierContext(root), cb);
    }
    return defaultOrderBy(root, cb);
  }

  public List<Expression<?>> groupBy(Root<?> root, CriteriaBuilder cb) {
    return null;
  }

  public List<Order> defaultOrderBy(Root<?> root, CriteriaBuilder cb) {
    return null;
  }

  public List<Predicate> buildPredicates(From<?, ?> root, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>(10);
    if (!CollectionUtils.isEmpty(getColumnQueries())) {
      this.getColumnQueries()
          .forEach(
              columnQuery -> {
                Optional<Predicate> predicate = columnQuery.build().createPredicate(root, cb);
                if (predicate.isPresent()) {
                  predicates.add(predicate.get());
                }
              });
    }

    if (root instanceof Root) {
      predicates.addAll(this.createExtPredicates((Root) root, cb));
    }

    return predicates;
  }

  public <J extends QueryBuilderPersistable, R extends QueryBuilderPersistable>
      Optional<Join<J, R>> getJoin(Class<J> jClass, Class<R> rClass, String attributeName) {
    if (joins != null && !joins.isEmpty()) {
      return Optional.of(
          (Join<J, R>) joins.get(RepositoryHelper.createJoinKey(jClass, rClass, attributeName)));
    }
    return Optional.empty();
  }

  private List<Predicate> createExtPredicates(Root root, CriteriaBuilder cb) {
    if (this.predicatesSupplier != null) {
      return this.predicatesSupplier.getPredicates(createQuerySupplierContext(root), cb);
    }
    return Collections.emptyList();
  }

  private QuerySupplierContext createQuerySupplierContext(Root root) {
    return QuerySupplierContext.builder()
        .root(root)
        .joins(this.joins == null ? Collections.emptyMap() : this.joins)
        .additionalRoots(
            this.additionalRoots == null ? Collections.emptyList() : this.additionalRoots)
        .build();
  }
}
