package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import java.util.List;

@FunctionalInterface
public interface GroupBySupplier {
  List<Expression<?>> groupBy(QuerySupplierContext context, CriteriaBuilder cb);
}
