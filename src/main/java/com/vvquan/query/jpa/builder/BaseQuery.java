package com.vvquan.query.jpa.builder;

import com.vvquan.query.jpa.builder.column.ColumnQuery;
import com.vvquan.query.jpa.builder.column.DateTimeColumn;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BaseQuery<P extends QueryBuilderPersistable> extends FromQuerySelections {

  boolean distinct;
  @Builder.Default boolean rootSelection = true;
  boolean countQuery;
  boolean ignoreAccessRight;
  JoinType accessRightJoinType;

  @Singular List<ColumnQuery.ColumnQueryBuilder> columnQueries;

  public void setCountQuery(boolean countQuery) {
    this.countQuery = countQuery;
  }

  public List<Root<?>> buildRoots(
      final Root<P> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb,
      final List<Predicate> predicates) {
    return this.buildRoots(root, query, cb, this, predicates);
  }

  public List<Root<?>> buildRoots(
      final Root<P> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb,
      final BaseQuery<?> baseQuery,
      final List<Predicate> predicates) {
    return Collections.emptyList();
  }

  public final Map<String, Join<?, ?>> buildJoins(
      final From<P, ?> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb,
      final List<Predicate> predicates) {
    return this.buildJoins(root, query, cb, this, predicates);
  }

  public Map<String, Join<?, ?>> buildJoins(
      final From<P, ?> root,
      final CriteriaQuery<?> query,
      final CriteriaBuilder cb,
      final BaseQuery<?> baseQuery,
      final List<Predicate> predicates) {
    return Collections.emptyMap();
  }

  public abstract static class BaseQueryBuilder<
          P extends QueryBuilderPersistable,
          C extends BaseQuery<P>,
          B extends BaseQueryBuilder<P, C, B>>
      extends FromQuerySelectionsBuilder<C, B> {

    private ColumnQuery.ColumnQueryBuilder lastColumnQuery() {
      return this.columnQueries.get(this.columnQueries.size() - 1);
    }

    private B updateLastColumnQuery(ColumnQuery.ColumnQueryBuilder last) {
      this.columnQueries.set(this.columnQueries.size() - 1, last);
      return self();
    }

    public B operator(BaseQueryConst.Operator operator) {
      return updateLastColumnQuery(lastColumnQuery().operator(operator));
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
