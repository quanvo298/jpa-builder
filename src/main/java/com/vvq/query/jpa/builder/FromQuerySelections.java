package com.vvq.query.jpa.builder;

import com.vvq.query.jpa.builder.column.ColumnQuery;
import com.vvq.query.jpa.builder.supplier.PredicatesSupplier;
import com.vvq.query.jpa.builder.supplier.SelectionsSupplier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  SelectionsSupplier selectionsSupplier;
  PredicatesSupplier predicatesSupplier;

  List<Selection<?>> multiSelections;
  Map<String, Join<?, ?>> joins;

  public abstract List<ColumnQuery.ColumnQueryBuilder> getColumnQueries();

  public List<Selection<?>> getMultiSelections() {
    return multiSelections == null ? Collections.emptyList() : multiSelections;
  }

  public BaseQueryConst.Junction getGlobalJunction() {
    return BaseQueryConst.Junction.And;
  }

  public void addMultiSelections(From<?, ?> root, CriteriaBuilder cb) {
    if (this.selectionsSupplier != null) {
      if (this.multiSelections == null) {
        this.multiSelections = new ArrayList<>(10);
      }
      this.multiSelections.addAll(this.selectionsSupplier.getSelections(root, cb, this.joins));
    }
  }

  public void addJoins(Map<String, Join<?, ?>> joins) {
    if (this.joins == null) {
      this.joins = new HashMap<>(5);
    }
    this.joins.putAll(joins);
  }

  public List<Expression<?>> groupBy(From<?, ?> root, CriteriaBuilder cb) {
    return null;
  }

  public List<Order> orderBy(From<?, ?> root, CriteriaBuilder cb) {
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

  private List<Predicate> createExtPredicates(Root root, CriteriaBuilder cb) {
    if (this.predicatesSupplier != null && !this.joins.isEmpty()) {
      return this.predicatesSupplier.getPredicates(root, cb, this.joins);
    }
    return Collections.emptyList();
  }
}
