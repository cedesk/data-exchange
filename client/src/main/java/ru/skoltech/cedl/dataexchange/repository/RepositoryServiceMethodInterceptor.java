/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
