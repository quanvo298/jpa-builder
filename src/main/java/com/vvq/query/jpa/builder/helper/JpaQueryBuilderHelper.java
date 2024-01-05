package com.vvq.query.jpa.builder.helper;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.resource.EntityQuery;
import com.vvq.query.jpa.builder.resource.JoinQuery;
import com.vvq.query.jpa.builder.resource.SupportedEntity;
import com.vvq.query.jpa.builder.supplier.ColumnsSupplier;
import com.vvq.query.jpa.builder.supplier.GroupBySupplier;
import com.vvq.query.jpa.builder.supplier.OrderBySupplier;
import com.vvq.query.jpa.builder.supplier.PredicatesSupplier;
import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import com.vvq.query.jpa.builder.utils.JpaQueryRepositoryUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

@Getter
public class JpaQueryBuilderHelper<P extends JpaQuery, B extends EntityQuery<P>> {
  private Root<P> root;
  private CriteriaQuery<?> query;
  private CriteriaBuilder cb;
  private Map<String, Join<? extends JpaQuery, ? extends JpaQuery>> joins;
  private B rootQuery;
  private Map<Class, SupportedEntity> supportedRoots;
  private List<Selection<?>> selections;
  private List<Predicate> predicates;

  public JpaQueryBuilderHelper(
      Root<P> root, CriteriaQuery<?> query, CriteriaBuilder cb, B rootQuery) {
    this.rootQuery = rootQuery;
    this.root = root;
    this.cb = cb;
    this.query = query;
    this.supportedRoots = new HashMap<>(5);
    this.selections = new ArrayList<>(10);
    this.joins = new HashMap<>();
    this.predicates =
        new ArrayList<>(
            CollectionUtils.isEmpty(this.rootQuery.getInitialPredicates())
                ? Collections.emptyList()
                : this.rootQuery.getInitialPredicates());
  }

  public void buildSupportedRoots() {
    List<SupportedEntity> supportedRoots = this.rootQuery.getEntitySupportedList();
    if (CollectionUtils.isNotEmpty(supportedRoots)) {
      this.supportedRoots =
          supportedRoots.stream()
              .collect(
                  Collectors.toMap(
                      SupportedEntity::getRootClazz,
                      query -> {
                        query.buildRoot(this.query);
                        return query;
                      }));
    }
  }

  public void buildJoins() {
    Map<String, JoinQuery> joinsMap = this.rootQuery.getJoinQueries();;
    for (String key : joinsMap.keySet()) {
      JoinQuery joinQuery = joinsMap.get(key);
      joinQuery.setSelectCount(this.rootQuery.isCountQuery());
      this.joins.putAll(JpaQueryRepositoryUtil.buildJoin(this.root, this.cb, key, joinQuery));
    }

    /*for (EntitySupportedQuery supported : this.supportedRoots.values()) {
      this.joins.putAll(supported.getJoins(this.cb));
    }*/
  }

  public void buildSelections() {
    ColumnsSupplier selectionsSupplier = this.rootQuery.getColumns();
    if (selectionsSupplier != null) {
      this.selections.addAll(selectionsSupplier.getExtraColumns(createQuerySupplierContext(), cb));
    }
  }

  public void buildGroupBy() {
    GroupBySupplier groupBySupplier = this.rootQuery.getGroupBySupplier();
    groupBySupplier = groupBySupplier != null ? groupBySupplier : this.rootQuery.groupByDefault();
    if (groupBySupplier != null) {
      List<Expression<?>> expressions =
          groupBySupplier.groupBy(createQuerySupplierContext(), this.cb);
      if (!CollectionUtils.isEmpty(expressions)) {
        this.query.groupBy(expressions);
      }
    }
  }

  public void buildOrderBy() {
    OrderBySupplier orderBySupplier = this.rootQuery.getOrderBySupplier();
    orderBySupplier = orderBySupplier != null ? orderBySupplier : this.rootQuery.orderByDefault();
    if (orderBySupplier != null) {
      List<Order> orderBy = orderBySupplier.getOrderBy(createQuerySupplierContext(), cb);
      if (!CollectionUtils.isEmpty(orderBy)) {
        query.orderBy(orderBy);
      }
    }
  }

  public void buildPredicates() {
    if (!CollectionUtils.isEmpty(this.rootQuery.getColumnQueries())) {
      this.rootQuery
          .getColumnQueries()
          .forEach(
              columnQuery -> {
                Optional<Predicate> predicate = columnQuery.build().createPredicate(root, cb);
                if (predicate.isPresent()) {
                  this.predicates.add(predicate.get());
                }
              });
    }

    QuerySupplierContext querySupplierContext = createQuerySupplierContext();
    this.predicates.addAll(this.createExtPredicates(querySupplierContext));

    for (SupportedEntity supported : this.supportedRoots.values()) {
      this.predicates.addAll(supported.getPredicates(querySupplierContext, cb));
    }
  }

  public List<Predicate> getPredicates() {
    return this.predicates.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  private List<Predicate> createExtPredicates(QuerySupplierContext querySupplierContext) {
    PredicatesSupplier predicatesSupplier = this.rootQuery.getPredicatesSupplier();
    if (predicatesSupplier != null) {
      return predicatesSupplier.getPredicates(querySupplierContext, cb);
    }
    return Collections.emptyList();
  }

  private QuerySupplierContext createQuerySupplierContext() {
    return QuerySupplierContext.builder()
        .root(root)
        .joins(joins)
        .supportedRoots(this.supportedRoots)
        .build();
  }
}
