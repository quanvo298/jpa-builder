package com.vvq.query.jpa.builder.column;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(builderMethodName = "internalBuilder")
public class LongColumn extends ColumnQuery<Long> {

  public static LongColumn.LongColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(From<?, ?> root, CriteriaBuilder cb, Path<Long> path) {
    List<Long> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (isSingle(valuesFiltered)) {
      Long value = this.getFirst(valuesFiltered);
      return this.createPredicateFromOperator(cb, path, value);
    }

    return path.in(valuesFiltered);
  }
}
