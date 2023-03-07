package com.vvq.query.jpa.builder.context;

import com.vvq.query.jpa.builder.QueryBuilderPersistable;
import com.vvq.query.jpa.builder.helper.RepositoryHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuerySupplierContext {
  private Root<?> root;
  private List<Root<?>> additionalRoots;
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

  public <R extends QueryBuilderPersistable> Optional<Root<?>> getAdditionalRoot(Class<R> rClass) {
    if (additionalRoots != null && !additionalRoots.isEmpty()) {
      return this.additionalRoots.stream().filter(r -> r.getJavaType() == rClass).findFirst();
    }
    return Optional.empty();
  }
}
