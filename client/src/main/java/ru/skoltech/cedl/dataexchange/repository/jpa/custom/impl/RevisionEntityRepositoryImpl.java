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

package ru.skoltech.cedl.dataexchange.repository.jpa.custom.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.repository.jpa.custom.RevisionEntityRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RevisionEntityRepositoryCustom}
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class RevisionEntityRepositoryImpl implements RevisionEntityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> List<Pair<CustomRevisionEntity, RevisionType>> findTaggedRevisions(Long id, Class<T> entityClass) {
        assert id != null;
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        List revisions = reader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return ((List<Object[]>) revisions).stream()
                .map(objects -> Pair.of((CustomRevisionEntity) objects[1], (RevisionType) objects[2]))
                .filter(triple -> triple.getLeft().getTag() != null)
                .collect(Collectors.toList());
    }

    @Override
    public CustomRevisionEntity lastRevisionEntity(Long id, Class entityClass) {
        assert id != null;
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        Object[] array = (Object[]) reader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(1)
                .getSingleResult();

        return (CustomRevisionEntity) array[1];
    }

    @Override
    public int lastRevisionNumber(Long id, Class entityClass) {
        return this.lastRevisionEntity(id, entityClass).getId();
    }

    @Override
    public <T> Pair<CustomRevisionEntity, RevisionType> lastRevision(Long id, Class<T> entityClass) {
        assert id != null;
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        Object[] array = (Object[]) reader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(1)
                .getSingleResult();

        return Pair.of((CustomRevisionEntity) array[1], (RevisionType) array[2]);
    }

}
