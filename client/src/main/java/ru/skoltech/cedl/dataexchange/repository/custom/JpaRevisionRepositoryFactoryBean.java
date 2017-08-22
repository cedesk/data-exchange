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

package ru.skoltech.cedl.dataexchange.repository.custom;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import ru.skoltech.cedl.dataexchange.repository.custom.impl.JpaRevisionEntityRepositoryImpl;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Extension of {@link JpaRepositoryFactoryBean} which register repositories of {@link JpaRevisionEntityRepository}.
 * <p>
 * Created by Nikolay Groshkov on 11-Aug-17.
 */
public class JpaRevisionRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> {

    /**
     * Creates a new {@link JpaRevisionRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public JpaRevisionRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new JpaRevisionRepositoryFactory(entityManager);
    }

    public static class JpaRevisionRepositoryFactory extends JpaRepositoryFactory {

        private BeanFactory beanFactory;

        private JpaRevisionRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            super.setBeanFactory(beanFactory);
            this.beanFactory = beanFactory;
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return JpaRevisionEntityRepositoryImpl.class;
        }

        @Override
        protected <T, ID extends Serializable> SimpleJpaRepository<T, ID> getTargetRepository(
                RepositoryInformation information, EntityManager entityManager) {
            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
            return getTargetRepositoryViaReflection(information, entityInformation, entityManager, beanFactory);
        }
    }
}
