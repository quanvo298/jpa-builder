package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.resource.QuerySupplierContext;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface PredicatesSupplier<T> {
  List<Predicate> getPredicates(QuerySupplierContext context, CriteriaBuilder cb);
}
