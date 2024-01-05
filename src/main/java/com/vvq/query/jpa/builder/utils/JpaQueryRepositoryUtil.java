package com.vvq.query.jpa.builder.utils;

import static java.util.Map.entry;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.JpaQueryConstant;
import com.vvq.query.jpa.builder.resource.EntityQuery;
import com.vvq.query.jpa.builder.resource.JoinQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

public class JpaQueryRepositoryUtil {
  public static <J extends JpaQuery, R extends JpaQuery, RQ extends EntityQuery<R>>
      Map<String, Join<J, R>> buildJoin(
          From<?, ?> root, CriteriaBuilder cb, String attributeName, JoinQuery<RQ> rq) {
    if (rq != null) {
      Map<String, Join<J, R>> joins = new HashMap<>();
      Join<J, R> joinInfo = buildJoinInfo(root, rq, attributeName);
      joins.put(createJoinKey(joinInfo, root, attributeName), joinInfo);
      List<Predicate> predicatesList = createJoinPredicates(rq.getResource(), joinInfo, cb);
      if (CollectionUtils.isNotEmpty(predicatesList)) {
        joinInfo.on(predicatesList.toArray(new Predicate[predicatesList.size()]));
      }
      joins.putAll(rq.getJoins(joinInfo, cb));
      return joins;
    }
    return Collections.emptyMap();
  }

  private static <J extends JpaQuery, R extends JpaQuery> Join<J, R> buildJoinInfo(
      From<?, ?> root, JoinQuery resource, String attributeName) {
    Join<J, R> joinInfo;
    JoinType joinType = resource.getJoinType();
    joinType = joinType == null ? JoinType.INNER : joinType;
    if (resource.isFetching()) {
      joinInfo = (Join<J, R>) (root.<J, R>fetch(attributeName, joinType));
    } else {
      joinInfo = root.join(attributeName, joinType);
    }
    return joinInfo;
  }

  public static Predicate buildJunction(
      CriteriaBuilder cb, List<Predicate> predicates, JpaQueryConstant.Junction globalJunction) {
    if (globalJunction == JpaQueryConstant.Junction.Or) {
      return cb.or(predicates.toArray(new Predicate[predicates.size()]));
    }
    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
  }

  public static Predicate buildJunction(CriteriaBuilder cb, List<Predicate> predicates) {
    return buildJunction(cb, predicates, JpaQueryConstant.Junction.And);
  }

  public static <P extends JpaQuery, R extends JpaQuery, RQ extends EntityQuery<R>>
      List<Predicate> createJoinPredicates(
          final RQ queryResource, final From<P, R> join, final CriteriaBuilder cb) {
    if (queryResource == null) {
      return Collections.emptyList();
    }
    List<Predicate> predicates =
        queryResource.getColumnQueries().stream()
            .map(columnQueryBuilder -> columnQueryBuilder.build().createPredicate(join, cb))
            .filter(Optional::isPresent)
            .map(Optional<Predicate>::get)
            .collect(Collectors.toList());
    return predicates;
  }

  public static String createJoinKey(Join<?, ?> joinInfo, From<?, ?> root, String attributeName) {
    return createJoinKey(joinInfo.getJavaType(), root.getJavaType(), attributeName);
  }

  public static String createJoinKey(Class jClass, Class rClass, String attributeName) {
    return new StringBuilder()
        .append(jClass.getName())
        .append(".")
        .append(attributeName)
        .append(".")
        .append(rClass.getName())
        .toString();
  }

  public static Map<String, JoinQuery> createJoinQueryImmutableMap(String key1, JoinQuery value1) {
    return filterAndGetJoinQueryImmutableMap(
        new Map.Entry[] {
          value1 != null ? entry(key1, value1) : null,
        });
  }

  public static Map<String, JoinQuery> createJoinQueryImmutableMap(
      String key1, JoinQuery value1, String key2, JoinQuery value2) {
    return filterAndGetJoinQueryImmutableMap(
        new Map.Entry[] {
          value1 != null ? entry(key1, value1) : null, value2 != null ? entry(key2, value2) : null,
        });
  }

  public static Map<String, JoinQuery> createJoinQueryImmutableMap(
      String key1, JoinQuery value1, String key2, JoinQuery value2, String key3, JoinQuery value3) {
    return filterAndGetJoinQueryImmutableMap(
        new Map.Entry[] {
          value1 != null ? entry(key1, value1) : null,
          value2 != null ? entry(key2, value2) : null,
          value3 != null ? entry(key3, value3) : null,
        });
  }

  public static Map<String, JoinQuery> createJoinQueryImmutableMap(
      String key1,
      JoinQuery value1,
      String key2,
      JoinQuery value2,
      String key3,
      JoinQuery value3,
      String key4,
      JoinQuery value4) {
    return filterAndGetJoinQueryImmutableMap(
        new Map.Entry[] {
          value1 != null ? entry(key1, value1) : null,
          value2 != null ? entry(key2, value2) : null,
          value3 != null ? entry(key3, value3) : null,
          value4 != null ? entry(key4, value4) : null,
        });
  }

  public static Map<String, JoinQuery> filterAndGetJoinQueryImmutableMap(
      Map.Entry<String, JoinQuery>[] entries) {
    return Map.ofEntries(
        Arrays.stream(entries).filter(Objects::nonNull).toArray(length -> new Map.Entry[length]));
  }
}
