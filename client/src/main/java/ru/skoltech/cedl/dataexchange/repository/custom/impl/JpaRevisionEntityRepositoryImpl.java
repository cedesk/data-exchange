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

package ru.skoltech.cedl.dataexchange.repository.custom.impl;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import ru.skoltech.cedl.dataexchange.entity.ModificationTimestamped;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.custom.JpaRevisionEntityRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Implemetation of {@link JpaRevisionEntityRepository}.
 * <p>
 * Created by Nikolay Groshkov on 10-Aug-17.
 */
public class JpaRevisionEntityRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements JpaRevisionEntityRepository<T, ID>, Serializable {

    private final EntityManager entityManager;
    private Class<T> entityClass;
    private ApplicationSettings applicationSettings;

    public JpaRevisionEntityRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                                           EntityManager entityManager,
                                           BeanFactory beanFactory) {
        super(entityInformation, entityManager);
        entityClass = entityInformation.getJavaType();
        this.entityManager = entityManager;
        this.applicationSettings = beanFactory.getBean(ApplicationSettings.class);
    }

    @Override
    public void delete(ID id) {
        super.delete(id);
        this.produceRevision();
    }

    @Override
    public void delete(T entity) {
        super.delete(entity);
        this.produceRevision();
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        super.delete(entities);
        this.produceRevision();
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        this.produceRevision();
    }

    @Override
    public void deleteAllInBatch() {
        super.deleteAllInBatch();
        this.produceRevision();
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        super.deleteInBatch(entities);
        this.produceRevision();
    }

    @Override
    public <S extends T> S save(S entity) {
        this.modificationTimestamped(entity);
        S newEntity = super.save(entity);
        this.produceRevision();
        return newEntity;
    }

    @Override
    public <S extends T> S save(S entity, String tag) {
        this.modificationTimestamped(entity);
        S newEntry = super.save(entity);
        this.produceRevision(tag);
        return newEntry;
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        this.modificationTimestamped(entity);
        S newEntity = super.saveAndFlush(entity);
        this.produceRevision();
        return newEntity;
    }

    @Override
    public <S extends T> S saveAndFlush(S entity, String tag) {
        this.modificationTimestamped(entity);
        S newEntry = super.saveAndFlush(entity);
        this.produceRevision(tag);
        return newEntry;
    }

    private void produceRevision() {
        this.produceRevision(null);
    }

    private void produceRevision(String tag) {
        if (!entityClass.isAnnotationPresent(Audited.class)) {
            return;
        }
        final AuditReader reader = AuditReaderFactory.get(entityManager);
        String username = applicationSettings.getProjectUserName();
        CustomRevisionEntity customRevisionEntity = reader.getCurrentRevision(CustomRevisionEntity.class, true);
        customRevisionEntity.setUsername(username);
        if (tag != null) {
            customRevisionEntity.setTag(tag);
        }
    }

    private <S extends T> void modificationTimestamped(S entity) {
        // Still necessary to save always study, because otherwise there is no new revision of it produced
        // and no changes noted
        if (entity instanceof ModificationTimestamped) {
            ModificationTimestamped modificationTimestamped = (ModificationTimestamped) entity;
            modificationTimestamped.setLastModification(System.currentTimeMillis());
        }
    }

}