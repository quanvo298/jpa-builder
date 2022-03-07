package com.vvquan.query.jpa.builder.column;

import com.vvquan.query.jpa.builder.BaseQueryConst;
import com.vvquan.query.jpa.builder.helper.RepositoryHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@SuperBuilder
public abstract class ColumnQuery<Y> {

  String columnName;

  Boolean isNotNull;

  boolean notOperator;

  BaseQueryConst.Operator operator;

  @Singular List<Y> values;

  public BaseQueryConst.Operator getOperator(boolean useDefault) {
    if (useDefault) {
      return this.operator == null ? BaseQueryConst.Operator.Equal : this.operator;
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
      CriteriaBuilder cb, Path<? extends Y> path, Y value, BaseQueryConst.Operator searchOperator) {
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
        return this.notOperator ? cb.notEqual(path, value) : cb.equal(path, value);
    }
  }

  public Predicate createPredicate(From<?, ?> root, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>(5);
    Path<Y> path = null;
    if (StringUtils.isNotEmpty(this.columnName)) {
      path = root.get(this.columnName);
      this.createPredicateNull(predicates, path);
    }
    Predicate predicate = createPredicatesByValues(root, cb, path);
    if (predicate != null) {
      predicates.add(predicate);
    }
    if (CollectionUtils.isEmpty(predicates)) {
      return null;
    }
    return RepositoryHelper.buildJunction(cb, predicates, BaseQueryConst.Junction.And);
  }

  public abstract Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<Y> path);
}
