package com.vvq.query.jpa.builder.column;

import com.vvq.query.jpa.builder.Operator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "internalBuilder")
@Getter
public class StringColumn extends ColumnQuery<String> {

  public static StringColumn.StringColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<String> path) {
    List<String> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }

    if (isSingle(values)) {
      String value = this.getFirst(valuesFiltered);
      return createSingleValue(cb, path, value);
    }

    return this.not ? cb.not(path.in(values)) : path.in(values);
  }

  private Predicate createSingleValue(CriteriaBuilder cb, Path<String> path, String value) {
    String wrapperValue = this.lowerCase ? value.toLowerCase() : value;
    Expression<String> wrapperPath = this.lowerCase ? cb.lower(path) : path;
    StringBuilder comparedValue = new StringBuilder(value.length() + 2);
    Operator correctOperator = this.getOperator(true);
    switch (correctOperator) {
      case StartLike:
        comparedValue = comparedValue.append("%").append(wrapperValue);
        break;
      case EndLike:
        comparedValue = comparedValue.append(wrapperValue).append("%");
        break;
      case Like:
        comparedValue = comparedValue.append("%").append(wrapperValue).append("%");
        break;
      default:
        comparedValue = comparedValue.append(wrapperValue);
        break;
    }
    if (correctOperator == Operator.Equal) {
      return this.not
          ? cb.notEqual(wrapperPath, value)
          : cb.equal(wrapperPath, comparedValue.toString());
    }
    return this.not
        ? cb.notLike(wrapperPath, value)
        : cb.like(wrapperPath, comparedValue.toString());
  }
}
