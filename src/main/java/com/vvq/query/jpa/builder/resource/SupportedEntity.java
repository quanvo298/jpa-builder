package com.vvq.query.jpa.builder.resource;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.supplier.context.QuerySupplierContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class SupportedEntity<R extends JpaQuery> {

  private Class<R> rootClazz;
  private Root<R> supportedRoot;
  private String rootPath;
  private String[] paths;

  private SupportedEntity(Class<R> rootClazz) {
    this.rootClazz = rootClazz;
  }
  // private PredicatesSupplier predicatesSupplier;

  public static <R extends JpaQuery> SupportedEntity<R> from(Class<R> clazz) {
    SupportedEntity entitySupported = new SupportedEntity(clazz);
    return entitySupported;
  }

  public void buildRoot(CriteriaQuery cq) {
    this.supportedRoot = cq.from(rootClazz);
  }

  public List<Predicate> getPredicates(QuerySupplierContext context, CriteriaBuilder cb) {
    /*if (predicatesSupplier != null) {
      return this.predicatesSupplier.getPredicates(context, cb);
    }*/
    if (rootPath != null && paths != null && paths.length > 0) {
      Path path = null;
      for (String fieldName : paths) {
        if (path == null) {
          path = this.supportedRoot.get(fieldName);
        } else {
          path = path.get(fieldName);
        }
      }

      return List.of(cb.equal(context.get(rootPath), path));
    }
    return Collections.emptyList();
  }

  public SupportedEntity rootField(String rootField) {
    this.rootPath = rootField;
    return this;
  }

  public SupportedEntity fields(String... fields) {
    this.paths = fields;
    return this;
  }
}
