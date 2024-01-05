package com.vvq.query.jpa.builder.resource;

import com.vvq.query.jpa.builder.JpaQuery;
import com.vvq.query.jpa.builder.utils.JpaQueryRepositoryUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinQuery<RQ extends EntityQuery<?>> {

  private boolean fetchInfo;
  private JoinType joinType;
  private RQ resource;
  private boolean selectCount;

  public void setSelectCount(boolean selectCount) {
    this.selectCount = selectCount;
    if (this.resource != null) {
      for (JoinQuery joinQuery : this.resource.joinQueries.values()) {
        joinQuery.setSelectCount(selectCount);
      }
    }
  }

  public boolean isFetching() {
    return !this.selectCount && this.fetchInfo;
  }

  public <J extends JpaQuery, R extends JpaQuery> Map<String, Join<J, R>> getJoins(
      From<?, ?> root, CriteriaBuilder cb) {
    Map<String, Join<J, R>> joins = new HashMap<>();
    if (this.resource != null) {
      Map<String, JoinQuery> joinsMap = this.resource.joinQueries;
      for (String key : joinsMap.keySet()) {
        JoinQuery joinQuery = joinsMap.get(key);
        if (joinQuery != null) {
          joins.putAll(JpaQueryRepositoryUtil.buildJoin(root, cb, key, joinQuery));
        }
      }
    }
    return joins;
  }
}
