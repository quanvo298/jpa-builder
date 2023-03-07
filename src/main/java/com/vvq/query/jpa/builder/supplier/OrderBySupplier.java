package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.context.QuerySupplierContext;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;

@FunctionalInterface
public interface OrderBySupplier {
  List<Order> getOrderBy(QuerySupplierContext context, CriteriaBuilder cb);
}
