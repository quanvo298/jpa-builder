package com.vvquan.query.jpa.builder.column;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "internalBuilder")
@Getter
public class IntegerColumn extends ColumnQuery<Integer> {

  public static IntegerColumn.IntegerColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<Integer> path) {
    List<Integer> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (isSingle(valuesFiltered)) {
      Integer value = this.getFirst(valuesFiltered);
      return this.createPredicateFromOperator(cb, path, value);
    }

    return path.in(valuesFiltered);
  }
}
