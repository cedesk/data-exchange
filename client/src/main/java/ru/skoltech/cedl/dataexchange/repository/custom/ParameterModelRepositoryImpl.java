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
import ru.skoltech.cedl.dataexchange.analysis.model.ParameterChange;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.repository.ParameterModelRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ParameterModelRepositoryCustom}.
 *
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class ParameterModelRepositoryImpl implements ParameterModelRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List findRevisionsOrderByRevisionNumberDesc(Long id) {
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        return reader.createQuery()
                .forRevisionsOfEntity(ParameterModel.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
    }

    @Override
    public List<ParameterChange> findAllParameterChangesOfSystem(long systemId) throws RepositoryException {
        List<ParameterChange> resultList = new ArrayList<>();
        try {
            Query nativeQuery = entityManager.createNativeQuery("SELECT " +
                    "rev_id, param_id, valueLink_id, node_id, timestamp, nature, valueSource, name, node_name " +
                    "FROM parameter_changes WHERE sys_id = " + systemId + " ORDER BY timestamp, node_id, nature ASC");
            List<Object[]> nativeQueryResultList = nativeQuery.getResultList();
            for (Object[] row : nativeQueryResultList) {
                ParameterChange pc = new ParameterChange(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]);
                resultList.add(pc);
            }
            return resultList;
        } catch (Exception e) {
            throw new RepositoryException("ParameterChange loading failed.", e);
        }
    }
}
