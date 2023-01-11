package com.vvq.query.jpa.dialect;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.BaseTupleQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements BaseRepository<T, ID> {

  private final EntityManager em;

  public BaseRepositoryImpl(
      final JpaEntityInformation<T, ?> entityInformation, final EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.em = entityManager;
  }

  public BaseRepositoryImpl(final Class<T> domainClass, final EntityManager entityManager) {
    super(domainClass, entityManager);
    this.em = entityManager;
  }

  protected static long executeCountQuery(TypedQuery<Long> query) {

    Assert.notNull(query, "TypedQuery must not be null!");

    List<Long> totals = query.getResultList();
    long total = 0L;

    for (Long element : totals) {
      total += element == null ? 0 : element;
    }

    return total;
  }

  @Override
  public <Q extends BaseTupleQuery, B extends BaseQuery>
      Q findOne(
          B resource,
          Specification<T> spec,
          PersistableTupleSupplier<Q> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    Tuple tuple = query.getSingleResult();
    if (tuple == null) {
      return null;
    }
    Q result = persistableFunctionSupplier.getFromTuple(tuple);
    return result;
  }

  @Override
  public <B extends BaseQuery> Page<T> findAll(
      B resource,
      Specification<T> spec,
      Pageable pageable,
      PersistableTupleSupplier<T> persistableFunctionSupplier) {
    Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
    Class<T> domainClass = getDomainClass();
    final TypedQuery<Tuple> query = getTypedQuery(resource, spec, domainClass, sort);

    if (pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }
    List<Tuple> tupleList = query.getResultList();
    List<T> result = new ArrayList<>(tupleList.size());
    tupleList.forEach(
        tuple -> {
          result.add(persistableFunctionSupplier.getFromTuple(tuple));
        });
    return PageableExecutionUtils.getPage(
        result, pageable, () -> executeCountQuery(getCountQuery(spec, domainClass)));
  }

  @Override
  public <B extends BaseQuery>
      List<T> findAll(
          B resource,
          Specification<T> spec,
          PersistableTupleSupplier<T> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    List<Tuple> tupleList = query.getResultList();
    List<T> result = new ArrayList<>(tupleList.size());
    tupleList.forEach(
        tuple -> {
          result.add(persistableFunctionSupplier.getFromTuple(tuple));
        });
    return result;
  }

  @Override
  public <Q extends BaseTupleQuery, B extends BaseQuery>
      List<Q> findAll(
          B resource,
          Specification<T> spec,
          PersistableTupleSupplier<Q> persistableFunctionSupplier,
          final int offset,
          final int maxResults) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must not be less than zero!");
    }
    if (maxResults < 1) {
      throw new IllegalArgumentException("Max results must not be less than one!");
    }

    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());

    query.setFirstResult(offset);
    query.setMaxResults(maxResults);
    List<Tuple> tupleList = query.getResultList();
    List<Q> result = new ArrayList<>(tupleList.size());
    tupleList.forEach(
        tuple -> {
          result.add(persistableFunctionSupplier.getFromTuple(tuple));
        });
    return result;
  }

  @Override
  public List<T> findAll(final Specification<T> spec, final int offset, final int maxResults) {
    return findAll(spec, offset, maxResults, Sort.unsorted());
  }

  @Override
  public List<T> findAll(
      final Specification<T> spec, final int offset, final int maxResults, final Sort sort) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must not be less than zero!");
    }
    if (maxResults < 1) {
      throw new IllegalArgumentException("Max results must not be less than one!");
    }

    final TypedQuery<T> query = getQuery(spec, sort);
    query.setFirstResult(offset);
    query.setMaxResults(maxResults);
    return query.getResultList();
  }

  protected <S> TypedQuery<Tuple> getTypedQuery(
      BaseQuery resource, @Nullable Specification<S> spec, Class<S> domainClass, Sort sort) {

    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> query = builder.createTupleQuery();

    Root<S> root = query.from(domainClass);
    Predicate predicate = spec.toPredicate(root, query, builder);

    if (predicate != null) {
      query.where(predicate);
    }

    List<Selection<?>> selections = new ArrayList<>(resource.getMultiSelections().size() + 1);
    if (resource.isRootSelection()) {
      selections.add(root.alias("_root"));
    }

    selections.addAll(resource.getMultiSelections());

    query.multiselect(selections).distinct(resource.isDistinct());
    if (sort.isSorted()) {
      query.orderBy(toOrders(sort, root, builder));
    }

    return em.createQuery(query);
  }
}
