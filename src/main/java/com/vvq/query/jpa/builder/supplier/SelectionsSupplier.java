package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.context.QuerySupplierContext;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

@FunctionalInterface
public interface SelectionsSupplier {
  List<Selection<?>> getSelections(QuerySupplierContext context, CriteriaBuilder cb);
}
