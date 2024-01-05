package com.vvq.query.jpa.builder.resource;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.Operator;
import com.vvq.query.jpa.builder.column.ColumnQuery;
import com.vvq.query.jpa.builder.column.DateTimeColumn;
import com.vvq.query.jpa.builder.helper.JpaQueryBuilderHelper;
import com.vvq.query.jpa.builder.supplier.AfterTuplePopulatedSupplier;
import com.vvq.query.jpa.builder.supplier.ColumnsSupplier;
import com.vvq.query.jpa.builder.supplier.GroupBySupplier;
import com.vvq.query.jpa.builder.supplier.OrOperator;
import com.vvq.query.jpa.builder.supplier.OrderBySupplier;
import com.vvq.query.jpa.builder.supplier.PredicatesSupplier;
import com.vvq.query.jpa.builder.utils.JpaQueryRepositoryUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.Specification;

@Getter
@SuperBuilder
public class EntityQuery<R extends JpaQuery> implements Specification<R> {
  OrderBySupplier orderBySupplier;
  GroupBySupplier groupBySupplier;
  ColumnsSupplier columns;
  PredicatesSupplier predicatesSupplier;
  AfterTuplePopulatedSupplier afterTuplePopulated;
  boolean countQuery;
  boolean distinct;
  boolean ignoreAccessRight;
  JoinType accessRightJoinType;

  @Singular("putJoin")
  Map<String, JoinQuery> joinQueries;

  @Builder.Default boolean rootSelection = true;
  @Builder.Default List<Predicate> initialPredicates = new ArrayList<>();

  @Singular("addEntitySupported")
  List<SupportedEntity> entitySupportedList;

  @Singular List<ColumnQuery.ColumnQueryBuilder> columnQueries;
  private List<Selection<?>> selections;

  public <J extends JpaQuery> Join<J, R> createAccessRightJoin(
      final Root<R> root, String attributeName) {

    Join<J, R> accessRightJoin =
        root.join(
            attributeName, accessRightJoinType != null ? accessRightJoinType : JoinType.INNER);

    /*this.initialJoins.put(
    JpaQueryRepositoryUtil.createJoinKey(accessRightJoin, root, attributeName),
    accessRightJoin);*/

    return accessRightJoin;
  }

  public void addPredicate(Predicate predicate) {
    this.initialPredicates.add(predicate);
  }

  public GroupBySupplier groupByDefault() {
    return null;
  }

  public OrderBySupplier orderByDefault() {
    return null;
  }

  public List<Selection<?>> getSelections() {
    return this.selections;
  }

  public boolean doJoinFetch() {
    return !countQuery
        && !this.joinQueries.isEmpty()
        && this.joinQueries.values().stream().allMatch(join -> join.isFetching());
  }

  @Override
  public Predicate toPredicate(
      Root<R> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    JpaQueryBuilderHelper jpaBaseQueryBuilder =
        new JpaQueryBuilderHelper<>(root, query, criteriaBuilder, this);
    jpaBaseQueryBuilder.buildSupportedRoots();
    jpaBaseQueryBuilder.buildJoins();
    jpaBaseQueryBuilder.buildSelections();
    jpaBaseQueryBuilder.buildPredicates();
    jpaBaseQueryBuilder.buildGroupBy();
    jpaBaseQueryBuilder.buildOrderBy();
    this.selections = jpaBaseQueryBuilder.getSelections();

    return JpaQueryRepositoryUtil.buildJunction(
        criteriaBuilder, jpaBaseQueryBuilder.getPredicates());
  }

  public abstract static class EntityQueryBuilder<
      R extends JpaQuery, C extends EntityQuery<R>, B extends EntityQueryBuilder<R, C, B>> {
    private ColumnQuery.ColumnQueryBuilder lastColumnQuery() {
      return this.columnQueries.get(this.columnQueries.size() - 1);
    }

    private B updateLastColumnQuery(ColumnQuery.ColumnQueryBuilder last) {
      this.columnQueries.set(this.columnQueries.size() - 1, last);
      return self();
    }

    public B supportedRoot(String rootField, SupportedEntity entitySupported) {
      /*if (this.entitySupportedList == null) {
        this.entitySupportedList = new ArrayList<>();
      }
      this.entitySupportedList.add(entitySupported.rootField(rootField));*/
      this.addEntitySupported(entitySupported.rootField(rootField));
      return self();
    }

    public B join(String fieldName, JoinQuery joinQuery) {
      this.putJoin(fieldName, joinQuery);
      return self();
    }

    public B operator(Operator operator) {
      return updateLastColumnQuery(lastColumnQuery().operator(operator));
    }

    public B greaterThanOrEqualTo(Object value) {
      return updateLastColumnQuery(
          lastColumnQuery().operator(Operator.GreaterAndEqual).values(Arrays.asList(value)));
    }

    public B greaterThan(Object value) {
      return updateLastColumnQuery(
          lastColumnQuery().operator(Operator.Greater).values(Arrays.asList(value)));
    }

    public B lessThanOrEqualTo(Object value) {
      return updateLastColumnQuery(
          lastColumnQuery().operator(Operator.LessAndEqual).values(Arrays.asList(value)));
    }

    public <EB extends EntityQuery.EntityQueryBuilder<?, ?, ?>> B or(OrOperator<EB> orOperator) {
      EB queryBuilder = orOperator.getColumnQueries();
      return updateLastColumnQuery(lastColumnQuery().orColumns(queryBuilder.build().columnQueries));
    }

    public B not() {
      return updateLastColumnQuery(lastColumnQuery().not(true));
    }

    public B orNull() {
      return updateLastColumnQuery(lastColumnQuery().orNull(true));
    }

    public B lower(String value) {
      return updateLastColumnQuery(lastColumnQuery().lowerCase(true).value(value));
    }

    public B between(LocalDateTime time, String start, String end) {
      if (time == null) {
        return self();
      }
      return self()
          .columnQuery(DateTimeColumn.internalBuilder().startCol(start).endCol(end).value(time));
    }

    public B between(LocalDateTime startTime, LocalDateTime endTime) {
      if (startTime == null || endTime == null) {
        return self();
      }
      return this.updateLastColumnQuery(
          ((DateTimeColumn.DateTimeColumnBuilder) lastColumnQuery())
              .startDate(startTime)
              .endDate(endTime));
    }
  }
}
