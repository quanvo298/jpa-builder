package com.vvq.query.jpa.builder.supplier.context;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.resource.SupportedEntity;
import com.vvq.query.jpa.builder.utils.JpaQueryRepositoryUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuerySupplierContext {
  private Root<? extends JpaQuery> root;
  private Map<Class, SupportedEntity> supportedRoots;
  private Map<String, Join<? extends JpaQuery, ? extends JpaQuery>> joins;

  public Path get(String path) {
    return root.get(path);
  }

  public <R extends JpaQuery> Path get(Class<R> rClass, String path) {
    return getSupportedRoot(rClass).get().get(path);
  }

  public <J extends JpaQuery, R extends JpaQuery> Optional<Join<J, R>> findJoin(
      Class<J> jClass, String attributeName) {
    if (joins != null && !joins.isEmpty()) {
      return Optional.of(
          (Join<J, R>)
              joins.get(
                  JpaQueryRepositoryUtil.createJoinKey(jClass, root.getJavaType(), attributeName)));
    }
    return Optional.empty();
  }
  public  <J extends JpaQuery, R extends JpaQuery> Join<J, R> getJoin(Class<J> jClass, String attributeName) {
    Optional<Join<J, R>> opt = findJoin(jClass, attributeName);
    return opt.get();
  }

  public <J extends JpaQuery, R extends JpaQuery> Optional<Join<J, R>> getJoin(
      Class<J> jClass, Class<R> rClass, String attributeName) {
    if (joins != null && !joins.isEmpty()) {
      return Optional.of(
          (Join<J, R>)
              joins.get(JpaQueryRepositoryUtil.createJoinKey(jClass, rClass, attributeName)));
    }
    return Optional.empty();
  }

  public <R extends JpaQuery> Optional<Root<R>> getSupportedRoot(Class<R> rClass) {
    SupportedEntity result = this.supportedRoots == null ? null : this.supportedRoots.get(rClass);
    if (result != null) {
      return Optional.of((Root<R>) result.getSupportedRoot());
    }
    return Optional.empty();
  }
}
