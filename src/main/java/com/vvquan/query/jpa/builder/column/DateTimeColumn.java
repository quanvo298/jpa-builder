package com.vvquan.query.jpa.builder.column;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(builderMethodName = "internalBuilder")
public class DateTimeColumn extends ColumnQuery<LocalDateTime> {

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  private String startCol;

  private String endCol;

  public static DateTimeColumnBuilder<?, ?> builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<LocalDateTime> path) {
    if (this.startDate != null && this.endDate != null) {
      return cb.between(path, this.startDate, this.endDate);
    }

    List<LocalDateTime> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }
    if (isSingle(valuesFiltered)) {
      LocalDateTime value = this.getFirst(valuesFiltered);
      if (this.startCol != null && this.endCol != null) {
        return cb.between(cb.literal(value), root.get(startCol), root.get(endCol));
      }
      return this.createPredicateFromOperator(cb, path, value);
    }

    return path.in(valuesFiltered);
  }
}
