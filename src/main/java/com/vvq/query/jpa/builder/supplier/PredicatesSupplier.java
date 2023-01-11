package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.QueryBuilderPersistable;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@FunctionalInterface
public interface PredicatesSupplier<T> {
  List<Predicate> getPredicates(Root root, CriteriaBuilder cb, Map<String, Join<? extends QueryBuilderPersistable, ? extends  QueryBuilderPersistable>> joins);
}
