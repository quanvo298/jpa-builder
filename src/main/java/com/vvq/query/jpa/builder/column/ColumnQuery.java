package com.vvq.query.jpa.builder.column;

import com.vvq.query.jpa.builder.JpaQueryConstant;
import com.vvq.query.jpa.builder.Operator;
import com.vvq.query.jpa.builder.utils.JpaQueryRepositoryUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Getter
@SuperBuilder
public abstract class ColumnQuery<Y> {

  String columnName;

  Boolean isNotNull;

  boolean orNull;

  boolean not;

  boolean lowerCase;

  Operator operator;

  @Singular List<Y> values;

  List<ColumnQuery.ColumnQueryBuilder> orColumns;

  public Operator getOperator(boolean useDefault) {
    if (useDefault) {
      return this.operator == null ? Operator.Equal : this.operator;
    }
    return this.operator;
  }

  void createPredicateNull(List<Predicate> predicates, Expression<?> path) {
    if (this.isNotNull != null) {
      predicates.add(this.isNotNull.booleanValue() ? path.isNotNull() : path.isNull());
    }
  }

  boolean isEmpty(List<Y> values) {
    return CollectionUtils.isEmpty(values);
  }

  boolean isNotEmpty(List<Y> values) {
    return !isEmpty(values);
  }

  boolean isSingle(List<Y> values) {
    return isNotEmpty(values) && values.size() == 1;
  }

  Y getFirst(List<Y> values) {
    return isNotEmpty(values) ? values.get(0) : null;
  }

  List<Y> filterNull() {
    return isNotEmpty(this.values)
        ? values.stream().filter(value -> value != null).collect(Collectors.toList())
        : null;
  }

  <Y extends Comparable<? super Y>> Predicate createPredicateFromOperator(
      CriteriaBuilder cb, Path<? extends Y> path, Y value) {
    return this.createPredicateFromOperator(cb, path, value, this.getOperator(true));
  }

  <Y extends Comparable<? super Y>> Predicate createPredicateFromOperator(
      CriteriaBuilder cb,
      Path<? extends Y> path,
      Y value,
      Operator searchOperator) {
    switch (searchOperator) {
      case Greater:
        return cb.greaterThan(path, value);
      case GreaterAndEqual:
        return cb.greaterThanOrEqualTo(path, value);
      case Less:
        return cb.lessThan(path, value);
      case LessAndEqual:
        return cb.lessThanOrEqualTo(path, value);
      case Equal:
      default:
        return this.not ? cb.notEqual(path, value) : cb.equal(path, value);
    }
  }

  public Optional<Predicate> createPredicate(From<?, ?> root, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>(5);
    Path<Y> path = null;
    if (StringUtils.isNotEmpty(this.columnName)) {
      path = root.get(this.columnName);
      this.createPredicateNull(predicates, path);
    }
    Predicate predicate = createPredicatesByValues(root, cb, path);
    if (predicate != null) {
      predicates.add(
          this.orNull
              ? JpaQueryRepositoryUtil.buildJunction(
                  cb, Arrays.asList(predicate, path.isNull()), JpaQueryConstant.Junction.Or)
              : predicate);
    }
    if (CollectionUtils.isEmpty(predicates)) {
      return Optional.empty();
    }
    Predicate result =
        JpaQueryRepositoryUtil.buildJunction(cb, predicates, JpaQueryConstant.Junction.And);

    if (!CollectionUtils.isEmpty(orColumns)) {
      Predicate orPredicate =
          JpaQueryRepositoryUtil.buildJunction(
              cb,
              orColumns.stream()
                  .map(
                      columnQuery -> {
                        Optional<Predicate> predicateOptional =
                            columnQuery.build().createPredicate(root, cb);
                        return predicateOptional.get();
                      })
                  .collect(Collectors.toList()));
      result =
          JpaQueryRepositoryUtil.buildJunction(
              cb, List.of(result, orPredicate), JpaQueryConstant.Junction.Or);
    }
    return Optional.of(result);
  }

  public abstract Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<Y> path);
}
