package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.BaseTupleQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

  <Q extends BaseTupleQuery, B extends BaseQuery> Q findOne(
      B resource, Specification<T> spec, PersistableTupleSupplier<Q> persistableFunctionSupplier);

  <B extends BaseQuery> Page<T> findAll(
      B resource,
      Specification<T> spec,
      Pageable pageable,
      PersistableTupleSupplier<T> persistableFunctionSupplier);

  <Q extends BaseTupleQuery, B extends BaseQuery> List<Q> findAll(
      B resource,
      Specification<T> spec,
      PersistableTupleSupplier<Q> persistableFunctionSupplier,
      int offset,
      int maxResults);

  <B extends BaseQuery> List<T> findAll(
        B resource, Specification<T> spec, PersistableTupleSupplier<T> persistableFunctionSupplier);

  List<T> findAll(Specification<T> spec, int offset, int maxResults, Sort sort);

  List<T> findAll(Specification<T> spec, int offset, int maxResults);
}
