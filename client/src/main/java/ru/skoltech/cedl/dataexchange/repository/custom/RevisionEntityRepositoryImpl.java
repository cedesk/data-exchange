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

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.repository.RevisionEntityRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Implementation of {@link RevisionEntityRepositoryCustom}
 *
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class RevisionEntityRepositoryImpl implements RevisionEntityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public CustomRevisionEntity lastCustomRevisionEntity(Long id, Class entityClass) {
        if (id == 0) {
            return null; // quick exit for unpersisted entities
        }
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        Object[] array = (Object[]) reader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(1)
                .getSingleResult();

        return (CustomRevisionEntity) array[1];
    }
}
