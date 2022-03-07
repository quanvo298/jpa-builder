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
