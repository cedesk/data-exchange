/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.repository;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.services.PersistenceRepositoryService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Arrays;

/**
 * Interceptor for {@link PersistenceRepositoryService}.
 * Based on passed {@link EntityManagerFactory} interceptor creates a new instance of {@link EntityManager}
 * before invocation of each method of {@link PersistenceRepositoryService} and close it after.
 *
 * Created by Nikolay Groshkov on 17-Jul-17.
 */
public class RepositoryServiceMethodInterceptor implements MethodInterceptor {

    private static Logger logger = Logger.getLogger(RepositoryServiceMethodInterceptor.class);

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
            return invocation.proceed();
        } catch (Throwable e) {
            logger.error("Invocation of method " + invocation.getMethod().getName() +
                    " with arguments " + Arrays.toString(invocation.getArguments()) +
                    " produces an exception: " + e.getMessage(), e);
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
