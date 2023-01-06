package com.vvq.query.jpa.builder;

import javax.persistence.criteria.JoinType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RelationshipQuery<T extends BaseQuery> {

  private boolean fetchInfo;

  private JoinType joinType;

  private T resource;
}
