package com.vvquan.query.jpa.builder.supplier;

import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Selection;

@FunctionalInterface
public interface SelectionsSupplier<T> {
  List<Selection<?>> getSelections(
      From<T, ?> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joins);
}
