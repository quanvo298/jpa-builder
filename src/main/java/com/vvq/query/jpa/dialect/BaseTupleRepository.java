package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.BaseTupleQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseTupleRepository<T> {

  <TP extends BaseTupleQuery, B extends BaseQuery> Optional<TP> findTupleOne(
      B resource, Specification<T> spec, PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <TP extends BaseTupleQuery, B extends BaseQuery> Page<TP> findTupleAll(
      B resource,
      Specification<T> spec,
      Pageable pageable,
      PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <TP extends BaseTupleQuery, B extends BaseQuery> List<TP> findTupleAll(
      B resource, Specification<T> spec, PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <TP extends BaseTupleQuery, B extends BaseQuery> List<TP> findTupleAll(
      B resource,
      Specification<T> spec,
      PersistableTupleSupplier<TP> persistableFunctionSupplier,
      int offset,
      int maxResults);
}
