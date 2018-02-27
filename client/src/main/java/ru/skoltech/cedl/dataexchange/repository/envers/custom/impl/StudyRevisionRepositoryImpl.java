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
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.repository.envers.custom.StudyRevisionRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Implementation of {@link StudyRevisionRepositoryCustom}.
 * <p>
 * Created by Nikolay Groshkov on 17-Aug-17.
 */
public class StudyRevisionRepositoryImpl implements StudyRevisionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Study findStudyByRevision(Long id, Integer revisionNumber) {
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        Study study = (Study) reader.createQuery()
                .forEntitiesModifiedAtRevision(Study.class, revisionNumber)
                .add(AuditEntity.id().eq(id))
                .getSingleResult();

        // intentionally traverse joined entities, not doing so produce an exception
        study.getSystemModel().externalModelsIterator().forEachRemaining(externalModel -> externalModel.getParent().getNodePath());
        study.getSystemModel().parametersTreeIterator().forEachRemaining(parameterModel -> {
            parameterModel.getValueSource();
            parameterModel.getNodePath();
            if (parameterModel.getUnit() != null) {
                parameterModel.getUnit().asText();
            }
            if (parameterModel.getValueLink() != null) {
                parameterModel.getValueLink().getNodePath();
            }
        });

        return study;
    }
}
