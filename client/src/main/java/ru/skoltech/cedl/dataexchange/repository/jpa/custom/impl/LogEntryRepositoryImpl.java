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

import ru.skoltech.cedl.dataexchange.entity.log.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.jpa.custom.LogEntryRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Implementation of {@link LogEntryRepositoryCustom}
 * <p>
 * Created by Dominik Knoll on 16-Aug-17.
 */
public class LogEntryRepositoryImpl implements LogEntryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LogEntry> getLogEntries(Long studyId) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogEntry> criteriaQuery = criteriaBuilder.createQuery(LogEntry.class);
        Root<LogEntry> root = criteriaQuery.from(LogEntry.class);
        if (studyId != null) {
            Predicate equal = criteriaBuilder.equal(root.get("studyId"), studyId);
            criteriaQuery = criteriaQuery.where(equal);
        }
        criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.asc(root.get("user")), criteriaBuilder.asc(root.get("id")));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
