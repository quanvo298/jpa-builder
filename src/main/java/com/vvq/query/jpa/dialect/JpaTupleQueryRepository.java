package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.builder.JpaTupleQuery;
import com.vvq.query.jpa.builder.resource.EntityQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaTupleQueryRepository<TP extends JpaTupleQuery> {
  <B extends EntityQuery> Optional<TP> findTupleOne(
      B resource, PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <B extends EntityQuery> Page<TP> findTupleAll(
      B resource,
      Pageable pageable,
      PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <B extends EntityQuery> List<TP> findTupleAll(
      B resource, PersistableTupleSupplier<TP> persistableFunctionSupplier);

  <B extends EntityQuery> List<TP> findTupleAll(
      B resource,
      PersistableTupleSupplier<TP> persistableFunctionSupplier,
      int offset,
      int maxResults);
}
