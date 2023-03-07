package com.vvq.query.jpa.dialect;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

import com.vvq.query.jpa.builder.BaseQuery;
import com.vvq.query.jpa.builder.BaseTupleQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import com.vvq.query.jpa.builder.supplier.paras.AfterTuplePopulateParas;
import com.vvq.query.jpa.builder.supplier.paras.WrapperTupleParas;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.springframework.util.CollectionUtils;

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
  public <B extends BaseQuery> Optional<T> findOne(
      B resource, Specification<T> spec, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    return this.getSingleResult(resource, query, persistableFunctionSupplier);
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

    List<T> result = getResultList(resource, query, persistableFunctionSupplier);
    return PageableExecutionUtils.getPage(
        result, pageable, () -> executeCountQuery(getCountQuery(spec, domainClass)));
  }

  @Override
  public <B extends BaseQuery> List<T> findAll(
      B resource, Specification<T> spec, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    List<T> result = getResultList(resource, query, persistableFunctionSupplier);
    return result;
  }

  @Override
  public <B extends BaseQuery> List<T> findAll(
      B resource,
      Specification<T> spec,
      PersistableTupleSupplier<T> persistableFunctionSupplier,
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
    List<T> result = getResultList(resource, query, persistableFunctionSupplier);
    return result;
  }

  @Override
  public <TP extends BaseTupleQuery, B extends BaseQuery> Optional<TP> findTupleOne(
      B resource, Specification<T> spec, PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    return this.getSingleTupleResult(resource, query, persistableFunctionSupplier);
  }

  @Override
  public <TP extends BaseTupleQuery, B extends BaseQuery> Page<TP> findTupleAll(
      B resource,
      Specification<T> spec,
      Pageable pageable,
      PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
    Class<T> domainClass = getDomainClass();
    final TypedQuery<Tuple> query = getTypedQuery(resource, spec, domainClass, sort);

    if (pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }
    List<TP> result = getTupleResultList(resource, query, persistableFunctionSupplier);

    return PageableExecutionUtils.getPage(
        result, pageable, () -> executeCountQuery(getCountQuery(spec, domainClass)));
  }

  @Override
  public <TP extends BaseTupleQuery, B extends BaseQuery> List<TP> findTupleAll(
      B resource, Specification<T> spec, PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query =
        getTypedQuery(resource, spec, getDomainClass(), Sort.unsorted());
    List<TP> result = getTupleResultList(resource, query, persistableFunctionSupplier);
    return result;
  }

  @Override
  public <TP extends BaseTupleQuery, B extends BaseQuery> List<TP> findTupleAll(
      B resource,
      Specification<T> spec,
      PersistableTupleSupplier<TP> persistableFunctionSupplier,
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
    List<TP> result = getTupleResultList(resource, query, persistableFunctionSupplier);
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

  private <TP extends BaseTupleQuery> List<TP> getTupleResultList(
      BaseQuery resource,
      TypedQuery<Tuple> query,
      PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    List<Tuple> tupleList = query.getResultList();
    List<TP> list =
        tupleList.stream()
            .map(
                tuple -> {
                  WrapperTupleParas wrapperTuple =
                      WrapperTupleParas.builder()
                          .currentTuple(tuple)
                          .tuples(Arrays.asList(tuple))
                          .build();
                  TP result = persistableFunctionSupplier.getFromTuple(wrapperTuple);
                  this.processAfterResult(resource, tuple, result, wrapperTuple.getRowIndex());
                  return result;
                })
            .collect(Collectors.toList());
    return list;
  }

  private List<T> getResultList(
      BaseQuery resource,
      TypedQuery<Tuple> query,
      PersistableTupleSupplier<T> persistableFunctionSupplier) {
    List<Tuple> tupleList = query.getResultList();
    List<T> list =
        tupleList.stream()
            .map(tuple -> this.processResult(tuple, resource, persistableFunctionSupplier))
            .collect(Collectors.toList());
    return list;
  }

  private Optional<T> getSingleResult(
      BaseQuery resource,
      TypedQuery<Tuple> query,
      PersistableTupleSupplier<T> persistableFunctionSupplier) {
    if (!resource.isSupportJoinFetch()) {
      Tuple tuple = query.getSingleResult();
      return Optional.of(this.processResult(tuple, resource, persistableFunctionSupplier));
    }
    List<Tuple> tupleList = query.getResultList();
    if (CollectionUtils.isEmpty(tupleList)) {
      return Optional.empty();
    }
    T result = this.processResult(tupleList.get(0), resource, persistableFunctionSupplier);
    for (int rowIndex = 1; rowIndex < tupleList.size(); rowIndex++) {
      Tuple tuple = tupleList.get(rowIndex);
      this.processAfterResult(resource, tuple, result, rowIndex);
    }
    return Optional.of(result);
  }

  private <TP extends BaseTupleQuery> Optional<TP> getSingleTupleResult(
      BaseQuery resource,
      TypedQuery<Tuple> query,
      PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    if (!resource.isSupportJoinFetch()) {
      Tuple tuple = query.getSingleResult();
      return Optional.of(this.processTupleResult(tuple, resource, persistableFunctionSupplier));
    }
    List<Tuple> tupleList = query.getResultList();
    if (CollectionUtils.isEmpty(tupleList)) {
      return Optional.empty();
    }
    TP result = this.processTupleResult(tupleList.get(0), resource, persistableFunctionSupplier);
    for (int rowIndex = 1; rowIndex < tupleList.size(); rowIndex++) {
      Tuple tuple = tupleList.get(rowIndex);
      this.processAfterResult(resource, tuple, result, rowIndex);
    }
    return Optional.of(result);
  }

  private T processResult(
      Tuple tuple, BaseQuery resource, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    WrapperTupleParas wrapperTuple =
        WrapperTupleParas.builder().currentTuple(tuple).tuples(Arrays.asList(tuple)).build();
    T result = persistableFunctionSupplier.getFromTuple(wrapperTuple);
    this.processAfterResult(resource, tuple, result, wrapperTuple.getRowIndex());
    return result;
  }

  private <TP extends BaseTupleQuery> TP processTupleResult(
      Tuple tuple, BaseQuery resource, PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    WrapperTupleParas wrapperTuple =
        WrapperTupleParas.builder().currentTuple(tuple).tuples(Arrays.asList(tuple)).build();
    TP result = persistableFunctionSupplier.getFromTuple(wrapperTuple);
    this.processAfterResult(resource, tuple, result, wrapperTuple.getRowIndex());
    return result;
  }

  private void processAfterResult(BaseQuery resource, Tuple tuple, Object result, int rowIndex) {
    if (resource.getAfterTuplePopulated() == null) {
      return;
    }
    resource
        .getAfterTuplePopulated()
        .afterGetFromTuple(
            AfterTuplePopulateParas.builder()
                .currentTuple(tuple)
                .rowIndex(rowIndex)
                .result(result)
                .build());
  }
}
