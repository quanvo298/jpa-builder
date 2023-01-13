package com.vvq.query.jpa.builder.resource;

import com.vvq.query.jpa.builder.QueryBuilderPersistable;
import com.vvq.query.jpa.builder.helper.RepositoryHelper;
import java.util.Map;
import java.util.Optional;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuerySelectionsContext {
  private From<?, ?> root;
  private Map<String, Join<? extends QueryBuilderPersistable, ? extends QueryBuilderPersistable>>
      joins;

  public <J extends QueryBuilderPersistable, R extends QueryBuilderPersistable>
      Optional<Join<J, R>> getJoin(Class<J> jClass, String attributeName) {
    if (joins != null && !joins.isEmpty()) {
      return Optional.of(
          (Join<J, R>)
              joins.get(RepositoryHelper.createJoinKey(jClass, root.getJavaType(), attributeName)));
    }
    return Optional.empty();
  }

  public <J extends QueryBuilderPersistable, R extends QueryBuilderPersistable>
      Optional<Join<J, R>> getJoin(Class<J> jClass, Class<R> rClass, String attributeName) {
    if (joins != null && !joins.isEmpty()) {
      return Optional.of(
          (Join<J, R>) joins.get(RepositoryHelper.createJoinKey(jClass, rClass, attributeName)));
    }
    return Optional.empty();
  }
}
