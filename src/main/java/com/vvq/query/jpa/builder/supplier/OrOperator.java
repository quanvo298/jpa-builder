package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.resource.EntityQuery;

@FunctionalInterface
public interface OrOperator<EB extends EntityQuery.EntityQueryBuilder<?, ?, ?>> {
  EB getColumnQueries();
}
