package com.vvq.query.jpa.builder.supplier;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

@FunctionalInterface
public interface SelectionFunctionSupplier<T> {
  List<Selection<?>> getExtSelections(From<T, ?> root, CriteriaBuilder cb, Path path);
}
