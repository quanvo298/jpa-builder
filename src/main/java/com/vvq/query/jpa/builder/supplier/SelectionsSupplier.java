package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.resource.QuerySelectionsContext;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

@FunctionalInterface
public interface SelectionsSupplier {
  List<Selection<?>> getSelections(QuerySelectionsContext context, CriteriaBuilder cb);
}
