package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Selection;
import java.util.List;

@FunctionalInterface
public interface ColumnsSupplier {
  List<Selection<?>> getExtraColumns(QuerySupplierContext context, CriteriaBuilder cb);
}
