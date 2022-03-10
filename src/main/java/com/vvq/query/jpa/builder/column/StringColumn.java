package com.vvq.query.jpa.builder.column;

import com.vvq.query.jpa.builder.BaseQueryConst;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
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

    return path.in(values);
  }

  private Predicate createSingleValue(CriteriaBuilder cb, Path<String> path, String value) {
    StringBuilder comparedValue = new StringBuilder(value.length() + 2);
    BaseQueryConst.Operator correctOperator = this.getOperator(true);
    switch (correctOperator) {
      case StartLike:
        comparedValue = comparedValue.append("%").append(value);
        break;
      case EndLike:
        comparedValue = comparedValue.append(value).append("%");
        break;
      case Like:
        comparedValue = comparedValue.append("%").append(value).append("%");
        break;
      default:
        comparedValue = comparedValue.append(value);
        break;
    }
    if (correctOperator == BaseQueryConst.Operator.Equal) {
      return this.notOperator ? cb.notEqual(path, value) : cb.equal(path, comparedValue.toString());
    }
    return this.notOperator ? cb.notLike(path, value) : cb.like(path, comparedValue.toString());
  }
}
