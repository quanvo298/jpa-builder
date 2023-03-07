package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable>
    extends JpaRepository<T, ID>, BaseTupleRepository<T> {

  <B extends BaseQuery> Optional<T> findOne(
      B resource, Specification<T> spec, PersistableTupleSupplier<T> persistableFunctionSupplier);

  <B extends BaseQuery> Page<T> findAll(
      B resource,
      Specification<T> spec,
      Pageable pageable,
      PersistableTupleSupplier<T> persistableFunctionSupplier);

  <B extends BaseQuery> List<T> findAll(
      B resource,
      Specification<T> spec,
      PersistableTupleSupplier<T> persistableFunctionSupplier,
      int offset,
      int maxResults);

  <B extends BaseQuery> List<T> findAll(
      B resource, Specification<T> spec, PersistableTupleSupplier<T> persistableFunctionSupplier);

  List<T> findAll(Specification<T> spec, int offset, int maxResults, Sort sort);

  List<T> findAll(Specification<T> spec, int offset, int maxResults);
}
