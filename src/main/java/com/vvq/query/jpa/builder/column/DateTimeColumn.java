package com.vvq.query.jpa.builder.column;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;
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
