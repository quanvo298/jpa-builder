package com.vvq.query.jpa.builder.context;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.QueryBuilderPersistable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuilderQueryContext<P extends QueryBuilderPersistable> {
  BaseQuery<P> baseQuery;
}
