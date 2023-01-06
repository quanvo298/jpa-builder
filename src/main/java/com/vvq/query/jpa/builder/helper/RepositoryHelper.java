package com.vvq.query.jpa.builder.helper;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.BaseQueryConst;
import com.vvq.query.jpa.builder.QueryBuilderPersistable;
import com.vvq.query.jpa.builder.RelationshipQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;

public class RepositoryHelper {

  public static <T, E> Join<T, E> buildJoinInfo(
      From<E, ?> root, RelationshipQuery resource, String attributeName, boolean ignoreFetch) {
    Join<T, E> joinInfo;
    JoinType joinType = resource.getJoinType();
    joinType = joinType == null ? JoinType.INNER : joinType;
    if (!ignoreFetch && resource.isFetchInfo()) {
      joinInfo = (Join<T, E>) (root.<T, E>fetch(attributeName, joinType));
    } else {
      joinInfo = root.<T, E>join(attributeName, joinType);
    }
    return joinInfo;
  }

  public static Predicate buildJunction(
      CriteriaBuilder cb, List<Predicate> predicates, BaseQueryConst.Junction globalJunction) {
    if (globalJunction == BaseQueryConst.Junction.Or) {
      return cb.or(predicates.toArray(new Predicate[predicates.size()]));
    }
    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
  }

  public static Predicate buildJunction(CriteriaBuilder cb, List<Predicate> predicates) {
    return buildJunction(cb, predicates, BaseQueryConst.Junction.And);
  }

  public static <P extends QueryBuilderPersistable, Q extends BaseQuery> Predicate createPredicate(
      final Q queryResource,
      final Root<P> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb) {
    return createPredicate(queryResource, root, query, cb, null);
  }

  public static <P extends QueryBuilderPersistable, Q extends BaseQuery> Predicate createPredicate(
      final Q queryResource,
      final Root<P> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb,
      final List<Predicate> initialPredicates) {
    List<Predicate> predicates = new ArrayList(10);
    if (!CollectionUtils.isEmpty(initialPredicates)) {
      predicates.addAll(initialPredicates);
    }

    if (queryResource == null) {
      return CollectionUtils.isEmpty(predicates)
          ? null
          : cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    queryResource.addMultiSelections(root, cb);
    List<Root<?>> roots = queryResource.buildRoots(root, query, cb, predicates);
    if (!CollectionUtils.isEmpty(roots)) {
      roots.forEach(exRoot -> queryResource.addMultiSelections(exRoot, cb));
    }

    Map<String, Join<?, ?>> joins = queryResource.buildJoins(root, query, cb, predicates);
    if (!joins.isEmpty()) {
      queryResource.addJoins(joins);
      joins.forEach((string, join) -> queryResource.addMultiSelections(join, cb));
    }

    List<Expression<?>> groupBy = queryResource.groupBy(root, cb);
    if (!CollectionUtils.isEmpty(groupBy)) {
      query.groupBy(groupBy);
    }

    List<Order> orderBy = queryResource.orderBy(root, cb);
    if (!CollectionUtils.isEmpty(orderBy)) {
      query.orderBy(orderBy);
    }

    predicates.addAll(queryResource.buildPredicates(root, cb));
    predicates =
        predicates.stream().filter(Objects::nonNull).collect(Collectors.toList());
    /*final List<Predicate> finalPredicates = new ArrayList(2);
    finalPredicates.add(cb.equal(root.get(EntityQuery.deleted), false));
    if (!CollectionUtils.isEmpty(predicates)) {
      finalPredicates.add(buildJunction(cb, predicates, queryResource.getGlobalJunction()));
    }*/
    return buildJunction(cb, predicates);
  }

  public static <
          T extends QueryBuilderPersistable,
          E extends QueryBuilderPersistable,
          Q extends BaseQuery<T>>
      Join<T, E> buildRelationship(
          From<E, ?> root,
          CriteriaQuery<?> query,
          final CriteriaBuilder cb,
          RelationshipQuery<Q> rq,
          String attributeName,
          boolean countQuery,
          Map<String, Join<?, ?>> joins,
          List<Predicate> predicates) {
    if (rq != null) {
      Join<T, E> joinInfo = RepositoryHelper.buildJoinInfo(root, rq, attributeName, countQuery);
      joins.put(createJoinKey(joinInfo.getJavaType(), root.getJavaType(), attributeName), joinInfo);
      Q qResource = rq.getResource();
      if (qResource != null && joinInfo != null) {
        List<Predicate> predicatesList = qResource.buildPredicates(joinInfo, cb);
        if (CollectionUtils.isNotEmpty(predicatesList)){
          joinInfo.on(predicatesList.toArray(new Predicate[predicatesList.size()]));
        }
        joins.putAll(qResource.buildJoins(joinInfo, query, cb, predicates));
      }
      return joinInfo;
    }
    return null;
  }

  public static Join<?, ?> getJoin(
      Class jClass, Class rClass, String attributeName, Map<String, Join<?, ?>> joins) {
    if (!joins.isEmpty()) {
      return joins.get(createJoinKey(jClass, rClass, attributeName));
    }
    return null;
  }

  private static String createJoinKey(Class jClass, Class rClass, String attributeName) {
    return new StringBuilder(50)
        .append(jClass.getName())
        .append(".")
        .append(attributeName)
        .append(".")
        .append(rClass.getName())
        .toString();
  }
}
