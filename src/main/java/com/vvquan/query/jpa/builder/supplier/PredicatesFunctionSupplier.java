package com.vvquan.query.jpa.builder.supplier;

import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@FunctionalInterface
public interface PredicatesFunctionSupplier<T> {
  List<Predicate> getExtPredicates(Root root, CriteriaBuilder cb, Map<Path, From<?, ?>> joins);
}
