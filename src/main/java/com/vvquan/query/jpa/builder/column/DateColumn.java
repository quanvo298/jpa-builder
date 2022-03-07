package com.vvquan.query.jpa.builder.column;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "internalBuilder")
@Getter
public class DateColumn extends ColumnQuery<LocalDate> {

  private LocalDate startDate;

  private LocalDate endDate;

  public static DateColumn.DateColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<LocalDate> path) {
    if (this.startDate != null && this.endDate != null) {
      return cb.between(path, this.startDate, this.endDate);
    }

    List<LocalDate> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (isSingle(valuesFiltered)) {
      LocalDate value = this.getFirst(valuesFiltered);
      return this.createPredicateFromOperator(cb, path, value);
    }

    return path.in(valuesFiltered);
  }
}
