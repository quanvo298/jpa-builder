package com.vvq.query.jpa.builder.column;

import com.vvq.query.jpa.builder.JpaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(builderMethodName = "internalBuilder")
@Getter
public class EntityColumn extends ColumnQuery<JpaQuery> {

  public static EntityColumn.EntityColumnBuilder builder(String name) {
    return internalBuilder().columnName(name);
  }

  public Predicate createPredicatesByValues(
      From<?, ?> root, CriteriaBuilder cb, Path<JpaQuery> path) {
    List<JpaQuery> valuesFiltered = this.filterNull();
    if (isEmpty(valuesFiltered)) {
      return null;
    }

    if (isSingle(values)) {
      JpaQuery value = this.getFirst(valuesFiltered);
      return createSingleValue(cb, path, value);
    }

    return this.not ? cb.not(path.in(values)) : path.in(values);
  }

  private Predicate createSingleValue(CriteriaBuilder cb, Path<JpaQuery> path, JpaQuery value) {
    return this.not ? cb.notEqual(path, value) : cb.equal(path, value);
  }
}
