package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.JpaTupleQuery;
import com.vvq.query.jpa.builder.resource.EntityQuery;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaQueryRepository<T extends JpaQuery & JpaTupleQuery, ID extends Serializable>
    extends JpaRepository<T, ID>, JpaTupleQueryRepository<T>, JpaSpecificationExecutor<T> {

  <B extends EntityQuery> Optional<T> findOneByQuery(B resource);

  List<T> findAll(Specification<T> spec, int offset, int maxResults, Sort sort);

  List<T> findAll(Specification<T> spec, int offset, int maxResults);
}
