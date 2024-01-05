package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

@FunctionalInterface
public interface PredicatesSupplier {
  List<Predicate> getPredicates(QuerySupplierContext context, CriteriaBuilder cb);
}
