package com.vvq.query.jpa.builder.column;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "internalBuilder")
@Getter
public class DoubleColumn extends ColumnQuery<Double> {

  public static DoubleColumn.DoubleColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<Double> path) {
    List<Double> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (isSingle(valuesFiltered)) {
      Double value = this.getFirst(valuesFiltered);
      return this.createPredicateFromOperator(cb, path, value);
    }

    return path.in(valuesFiltered);
  }
}
