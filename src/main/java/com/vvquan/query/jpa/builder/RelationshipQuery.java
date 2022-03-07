package com.vvquan.query.jpa.builder;

import javax.persistence.criteria.JoinType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RelationshipQuery<T extends BaseQuery> {

  private boolean fetchInfo;

  private JoinType joinType;

  private T resource;
}
