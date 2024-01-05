package com.vvq.query.jpa.dialect.impl;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.JpaTupleQuery;
import com.vvq.query.jpa.builder.resource.EntityQuery;
import com.vvq.query.jpa.builder.supplier.PersistableTupleSupplier;
import com.vvq.query.jpa.builder.supplier.paras.AfterTuplePopulateParas;
import com.vvq.query.jpa.builder.supplier.paras.WrapperTupleParas;
import com.vvq.query.jpa.dialect.JpaQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class JpaQueryRepositoryImpl<T extends JpaQuery & JpaTupleQuery, ID extends Serializable>
    extends SimpleJpaRepository<T, ID> implements JpaQueryRepository<T, ID> {

  private final EntityManager em;

  public JpaQueryRepositoryImpl(
      final JpaEntityInformation<T, ?> entityInformation, final EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.em = entityManager;
  }

  public JpaQueryRepositoryImpl(final Class<T> domainClass, final EntityManager entityManager) {
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
  public <B extends EntityQuery> Optional<T> findOneByQuery(B resource) {
    final TypedQuery<Tuple> query = getTypedQuery(resource, getDomainClass(), Sort.unsorted());
    if (!resource.doJoinFetch()) {
      query.setMaxResults(1);
    }

    List<Tuple> tupleList = query.getResultList();
    if (CollectionUtils.isEmpty(tupleList)) {
      return Optional.empty();
    }

    T result = (T) tupleList.get(0).get("_root");
    return Optional.of(result);
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

  @Override
  public <B extends EntityQuery> Optional<T> findTupleOne(
      B resource, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query = getTypedQuery(resource, getDomainClass(), Sort.unsorted());
    return this.getSingleTupleResult(resource, query, persistableFunctionSupplier);
  }

  @Override
  public <B extends EntityQuery> Page<T> findTupleAll(
      B resource, Pageable pageable, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    Sort sort = pageable.isPaged() ? pageable.getSort() : Sort.unsorted();
    Class<T> domainClass = getDomainClass();
    final TypedQuery<Tuple> query = getTypedQuery(resource, domainClass, sort);

    if (pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }
    List<T> result = getTupleResultList(resource, query, persistableFunctionSupplier);

    return PageableExecutionUtils.getPage(
        result, pageable, () -> executeCountQuery(getCountQuery(resource, domainClass)));
  }

  @Override
  public <B extends EntityQuery> List<T> findTupleAll(
      B resource, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    final TypedQuery<Tuple> query = getTypedQuery(resource, getDomainClass(), Sort.unsorted());
    List<T> result = getTupleResultList(resource, query, persistableFunctionSupplier);
    return result;
  }

  @Override
  public <B extends EntityQuery> List<T> findTupleAll(
      B resource,
      PersistableTupleSupplier<T> persistableFunctionSupplier,
      final int offset,
      final int maxResults) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must not be less than zero!");
    }
    if (maxResults < 1) {
      throw new IllegalArgumentException("Max results must not be less than one!");
    }

    final TypedQuery<Tuple> query = getTypedQuery(resource, getDomainClass(), Sort.unsorted());

    query.setFirstResult(offset);
    query.setMaxResults(maxResults);
    List<T> result = getTupleResultList(resource, query, persistableFunctionSupplier);
    return result;
  }

  protected <S> TypedQuery<Tuple> getTypedQuery(
     EntityQuery resource, Class<S> domainClass, Sort sort) {

    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> query = builder.createTupleQuery();

    Root<S> root = query.from(domainClass);
    Predicate predicate = resource.toPredicate(root, query, builder);

    if (predicate != null) {
      query.where(predicate);
    }

    List<Selection<?>> selections = new ArrayList<>(resource.getSelections().size() + 1);
    if (resource.isRootSelection()) {
      selections.add(root.alias("_root"));
    }
    selections.addAll(resource.getSelections());
    query.multiselect(selections).distinct(resource.isDistinct());
    if (sort.isSorted()) {
      query.orderBy(toOrders(sort, root, builder));
    }

    return em.createQuery(query);
  }

  private <TP extends JpaTupleQuery> List<TP> getTupleResultList(
      EntityQuery resource,
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

  private <TP extends JpaTupleQuery> Optional<TP> getSingleTupleResult(
      EntityQuery resource,
      TypedQuery<Tuple> query,
      PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    if (!resource.doJoinFetch()) {
      query.setMaxResults(1);
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
     Tuple tuple, EntityQuery resource, PersistableTupleSupplier<T> persistableFunctionSupplier) {
    WrapperTupleParas wrapperTuple =
        WrapperTupleParas.builder().currentTuple(tuple).tuples(Arrays.asList(tuple)).build();
    T result = persistableFunctionSupplier.getFromTuple(wrapperTuple);
    this.processAfterResult(resource, tuple, result, wrapperTuple.getRowIndex());
    return result;
  }

  private <TP extends JpaTupleQuery> TP processTupleResult(
     Tuple tuple, EntityQuery resource, PersistableTupleSupplier<TP> persistableFunctionSupplier) {
    WrapperTupleParas wrapperTuple =
        WrapperTupleParas.builder().currentTuple(tuple).tuples(Arrays.asList(tuple)).build();
    TP result = persistableFunctionSupplier.getFromTuple(wrapperTuple);
    this.processAfterResult(resource, tuple, result, wrapperTuple.getRowIndex());
    return result;
  }

  private void processAfterResult(EntityQuery resource, Tuple tuple, Object result, int rowIndex) {
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
