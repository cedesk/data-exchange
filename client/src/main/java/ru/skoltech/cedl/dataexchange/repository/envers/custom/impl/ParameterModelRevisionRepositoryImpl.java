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

package ru.skoltech.cedl.dataexchange.repository.envers.custom.impl;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.repository.envers.custom.ParameterModelRevisionRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ParameterModelRevisionRepositoryCustom}.
 *
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class ParameterModelRevisionRepositoryImpl implements ParameterModelRevisionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ParameterRevision> findParameterRevisionsOrderByRevisionNumberDesc(Long id) {
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        List revisions = reader.createQuery()
                .forRevisionsOfEntity(ParameterModel.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        List<ParameterRevision> parameterRevisions = ((List<Object[]>) revisions).stream()
                .map(revision -> new ParameterRevision((ParameterModel) revision[0], (CustomRevisionEntity) revision[1]))
                .collect(Collectors.toList());

        // TODO: intentionally traverse joined entities, not doing so produce an exception
        // must be fixed somehow
        parameterRevisions.forEach(ParameterRevision::getUnitAsText);
        parameterRevisions.forEach(ParameterRevision::getNodePath);
        parameterRevisions.forEach(ParameterRevision::getValueLink);
        parameterRevisions.forEach(parameterRevision -> {
            if (parameterRevision.getValueLink() != null) {
                parameterRevision.getValueLink().getNodePath();
            }
        });
        parameterRevisions.forEach(ParameterRevision::getSourceDetails);

        return parameterRevisions;
    }
}
