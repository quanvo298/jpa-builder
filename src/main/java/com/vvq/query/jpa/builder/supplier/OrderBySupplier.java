package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import java.util.List;

@FunctionalInterface
public interface OrderBySupplier {
  List<Order> getOrderBy(QuerySupplierContext context, CriteriaBuilder cb);
}
