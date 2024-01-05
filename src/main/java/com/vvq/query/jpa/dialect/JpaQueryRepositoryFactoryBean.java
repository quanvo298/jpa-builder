package com.vvq.query.jpa.dialect;

import com.vvq.query.jpa.dialect.impl.JpaQueryRepositoryImpl;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class JpaQueryRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
    extends JpaRepositoryFactoryBean<R, T, I> {

  public JpaQueryRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
    super(repositoryInterface);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected RepositoryFactorySupport createRepositoryFactory(final EntityManager entityManager) {
    return new JpaQueryRepositoryFactory(entityManager);
  }

  private static class JpaQueryRepositoryFactory<T, I extends Serializable>
      extends JpaRepositoryFactory {

    public JpaQueryRepositoryFactory(final EntityManager em) {
      super(em);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "hiding"})
    protected <T, ID extends Serializable> SimpleJpaRepository<?, ?> getTargetRepository(
        final RepositoryMetadata metadata, final EntityManager entityManager) {
      final SimpleJpaRepository<?, ?> repo =
          new JpaQueryRepositoryImpl(metadata.getDomainType(), entityManager);
      return repo;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(final RepositoryMetadata metadata) {
      return JpaQueryRepositoryImpl.class;
    }
  }
}
