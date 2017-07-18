package ru.skoltech.cedl.dataexchange.repository;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.skoltech.cedl.dataexchange.services.PersistenceRepositoryService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Interceptor for {@link PersistenceRepositoryService}.
 * Based on passed {@link EntityManagerFactory} interceptor creates a new instance of {@link EntityManager}
 * before invocation of each method of {@link PersistenceRepositoryService} and close it after.
 *
 * Created by n.groshkov on 17-Jul-17.
 */
public class RepositoryServiceMethodInterceptor implements MethodInterceptor {

    private EntityManagerFactory entityManagerFactory;

    /**
     * Receive a new {@link EntityManagerFactory}.
     * Must be invoked after changing actual persistence unit.
     *
     * @param entityManagerFactory actual {@link EntityManagerFactory}
     */
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ((PersistenceRepositoryService)invocation.getThis()).setEntityManager(entityManager);
            Object result = invocation.proceed();
            return result;
        } catch (Throwable e) {
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
