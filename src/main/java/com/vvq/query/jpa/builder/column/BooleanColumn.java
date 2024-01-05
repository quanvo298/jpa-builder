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
public class BooleanColumn extends ColumnQuery<Boolean> {

  public static BooleanColumn.BooleanColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<Boolean> path) {
    List<Boolean> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (this.isSingle(valuesFiltered)) {
      return this.getFirst(valuesFiltered) == true ? cb.isTrue(path) : cb.isFalse(path);
    }

    return null;
  }
}
