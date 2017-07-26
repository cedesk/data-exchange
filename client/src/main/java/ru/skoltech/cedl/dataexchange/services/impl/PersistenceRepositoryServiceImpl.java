/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.analysis.model.ParameterChange;
import ru.skoltech.cedl.dataexchange.db.ApplicationProperty;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.PersistenceRepositoryService;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link PersistenceRepositoryService}.
 * <p>
 * Created by dknoll on 24/05/15.
 */
public class PersistenceRepositoryServiceImpl implements PersistenceRepositoryService {

    private static final Logger logger = Logger.getLogger(PersistenceRepositoryServiceImpl.class);

    private static final long SCHEME_VERSION_APPLICATION_PROPERTY_ID = 1;
    private static final String SCHEME_VERSION_APPLICATION_PROPERTY_NAME = "version";

    private EntityManager entityManager;

    @Override
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public String toString() {
        return "PersistenceRepositoryServiceImpl{" +
                entityManager.getProperties() +
                "}";
    }

    @Override
    public List<String> listStudies() throws RepositoryException {
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
            Root<Study> root = criteriaQuery.from(Study.class);
            criteriaQuery.select(root.get("name"));
            return entityManager.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Study loading failed.", e);
        }
    }

    @Override
    public Study loadStudy(String name) throws RepositoryException {
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Study.class);
            final Root root = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(root.get("name"), name);
            criteriaQuery.where(namePredicate);
            final TypedQuery query = entityManager.createQuery(criteriaQuery);
            Object singleResult = query.getSingleResult();
//            entityManager.refresh(singleResult);
            return (Study) singleResult;
        } catch (NoResultException e) {
            throw new RepositoryException("Study not found.", e);
        } catch (Exception e) {
            throw new RepositoryException("Study loading failed.", e);
        }
    }

    @Override
    public void deleteStudy(String name) throws RepositoryException {
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Study.class);
            final Root studyRoot = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(studyRoot.get("name"), name);
            criteriaQuery.where(namePredicate);
            final TypedQuery query = entityManager.createQuery(criteriaQuery);
            Object singleResult = query.getSingleResult();
            entityManager.remove(singleResult);
        } catch (Exception e) {
            throw new RepositoryException("Deleting Study failed.", e);
        }
    }

    @Override
    public Study storeStudy(Study study) throws RepositoryException {
        try {
            entityManager.setFlushMode(FlushModeType.AUTO);
            if (study.getId() == 0) {
                entityManager.persist(study);
            } else {
                study = entityManager.merge(study);
            }
            study = entityManager.find(Study.class, study.getId());
        } catch (OptimisticLockException | RollbackException re) {
            logger.warn("transaction failed", re);
            throw extractAndRepackCause(re);
        } catch (Exception e) {
            throw new RepositoryException("Storing Study failed.", e);
        }
        return study;
    }

    @Override
    public SystemModel loadSystemModel(long systemModelId) throws RepositoryException {
        SystemModel systemModel;
        try {
            systemModel = entityManager.find(SystemModel.class, systemModelId);
        } catch (Exception e) {
            throw new RepositoryException("Loading SystemModel failed.", e);
        }
        if (systemModel == null)
            throw new RepositoryException("SystemModel not found.");
        return systemModel;
    }

    @Override
    public SystemModel storeSystemModel(SystemModel modelNode) throws RepositoryException {
        try {
            if (modelNode.getId() == 0) {
                entityManager.persist(modelNode);
            } else {
                modelNode = entityManager.merge(modelNode);
            }
        } catch (Exception e) {
            throw new RepositoryException("Storing SystemModel failed.", e);
        }
        return modelNode;
    }

    @Override
    public ExternalModel loadExternalModel(long externalModelId) throws RepositoryException {
        try {
            ExternalModel externalModel = entityManager.find(ExternalModel.class, externalModelId);
            //entityManager.refresh(externalModel);
            if (externalModel == null) {
                throw new RepositoryException("ExternalModel not found.");
            }
            return externalModel;
        } catch (Exception e) {
            throw new RepositoryException("Loading ExternalModel failed.", e);
        }
    }

    @Override
    public ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException {
        try {
            if (externalModel.getId() == 0) {
                entityManager.persist(externalModel);
            } else {
                externalModel = entityManager.merge(externalModel);
            }
        } catch (Exception e) {
            throw new RepositoryException("Storing ExternalModel failed.", e);
        }
        return externalModel;
    }

    @Override
    public UserRoleManagement loadUserRoleManagement(long id) throws RepositoryException {
        try {
            UserRoleManagement userRoleManagement = entityManager.find(UserRoleManagement.class, id);
            if (userRoleManagement == null) {
                throw new RepositoryException("UserRoleManagement not found.");
            }
            return userRoleManagement;
        } catch (Exception e) {
            throw new RepositoryException("Loading UserRoleManagement failed.", e);
        }
    }

    @Override
    public UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws
            RepositoryException {
        try {
            if (userRoleManagement.getId() == 0) {
                entityManager.persist(userRoleManagement);
            } else {
                userRoleManagement = entityManager.merge(userRoleManagement);
            }
        } catch (Exception e) {
            throw new RepositoryException("Storing UserRoleManagement failed.", e);
        }
        return userRoleManagement;
    }

    @Override
    public UserManagement loadUserManagement() throws RepositoryException {
        UserManagement userManagement;
        try {
            userManagement = entityManager.find(UserManagement.class, UserManagementService.IDENTIFIER);
        } catch (Exception e) {
            throw new RepositoryException("Loading UserManagement failed.", e);
        }
        if (userManagement == null)
            throw new RepositoryException("UserManagement not found.");
        return userManagement;
    }

    @Override
    public UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException {
        try {
            if (userManagement.getId() == 0) {
                entityManager.persist(userManagement);
            } else {
                userManagement = entityManager.merge(userManagement);
            }
        } catch (Exception e) {
            throw new RepositoryException("Storing UserManagement failed.", e);
        }
        return userManagement;
    }

    @Override
    public UnitManagement loadUnitManagement() throws RepositoryException {
        UnitManagement unitManagement;
        try {
            unitManagement = entityManager.find(UnitManagement.class, UnitManagementService.IDENTIFIER);
        } catch (Exception e) {
            throw new RepositoryException("Loading UnitManagement failed.", e);
        }
        if (unitManagement == null)
            throw new RepositoryException("UnitManagement not found.");
        return unitManagement;
    }

    @Override
    public UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException {
        try {
            if (unitManagement.getId() == 0) {
                entityManager.persist(unitManagement);
            } else {
                unitManagement = entityManager.merge(unitManagement);
            }
        } catch (Exception e) {
            throw new RepositoryException("Storing UnitManagement failed.", e);
        }
        return unitManagement;
    }

    public List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException {
        try {
            final AuditReader reader = AuditReaderFactory.get(entityManager);
            final long pk = parameterModel.getId();

            List<Object[]> revisions = reader.createQuery()
                    .forRevisionsOfEntity(ParameterModel.class, false, true)
                    .add(AuditEntity.id().eq(pk))
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .getResultList();

            List<ParameterRevision> revisionList = new ArrayList<>(revisions.size());
            for (Object[] array : revisions) {
                ParameterModel versionedParameterModel = (ParameterModel) array[0];
                CustomRevisionEntity revisionEntity = (CustomRevisionEntity) array[1];
                RevisionType revisionType = (RevisionType) array[2];

                ParameterRevision parameterRevision = new ParameterRevision(versionedParameterModel, revisionEntity, revisionType);
                try {
                    // dummy operation to ensure properties to be loaded (overcome lazy loading, which would fail due to closed db connection)
                    parameterRevision.toString().length();
                } catch (Exception e) {
                    logger.error("problem initializing parameter revision properties, " + e.getMessage());
                }
                revisionList.add(parameterRevision);
            }

            return revisionList;
        } catch (Exception e) {
            throw new RepositoryException("Loading revision history failed.", e);
        }
    }

    public CustomRevisionEntity getLastRevision(PersistedEntity persistedEntity) {
        try {
            final long pk = persistedEntity.getId();
            if (pk == 0) return null; // quick exit for unpersisted entities
            final AuditReader reader = AuditReaderFactory.get(entityManager);

            Object[] array = (Object[]) reader.createQuery()
                    .forRevisionsOfEntity(persistedEntity.getClass(), false, true)
                    .add(AuditEntity.id().eq(pk))
                    .addOrder(AuditEntity.revisionNumber().desc()).setMaxResults(1)
                    .getSingleResult();

            return (CustomRevisionEntity) array[1];
        } catch (Exception e) {
            logger.debug("Loading revision history failed: " +
                    persistedEntity.getClass().getSimpleName() + "[" + persistedEntity.getId() + "]");
            return null;
        }
    }

    @Override
    public List<ParameterChange> getChanges(long systemId) throws RepositoryException {
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

    @Override
    public List<LogEntry> getLogEntries(Long fromId, Long toId) throws RepositoryException {
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<LogEntry> criteriaQuery = criteriaBuilder.createQuery(LogEntry.class);
            Root<LogEntry> root = criteriaQuery.from(LogEntry.class);
            if (fromId != null && toId != null) {
                Predicate greaterThanOrEqualTo = criteriaBuilder.greaterThanOrEqualTo(root.get("id"), fromId);
                Predicate lessThanOrEqualTo = criteriaBuilder.lessThanOrEqualTo(root.get("id"), toId);
                criteriaQuery = criteriaQuery.where(greaterThanOrEqualTo, lessThanOrEqualTo);
            }
            criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.asc(root.get("user")), criteriaBuilder.asc(root.get("id")));
            return entityManager.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Study loading LogEntries.", e);
        }
    }

    @Override
    public void storeLog(LogEntry logEntry) {
        try {
            entityManager.persist(logEntry);
        } catch (Exception e) {
            logger.debug("logging action to database failed: " + e.getMessage());
        }
    }

    @Override
    public Long getLastStudyModification(String name) {
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
            final Root<Study> root = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(root.get("name"), name);
            criteriaQuery.where(namePredicate);
            criteriaQuery.select(criteriaBuilder.tuple(root.get("latestModelModification")));
            Tuple result = entityManager.createQuery(criteriaQuery).getSingleResult();
            return (Long) result.get(0);
        } catch (NoResultException e) {
            logger.warn("study not stored!");
            return null;
        } catch (Exception e) {
            logger.warn("loading last modification of study failed.", e);
            return null;
        }
    }

    private RepositoryException extractAndRepackCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && !(cause instanceof StaleObjectStateException)) {
            cause = cause.getCause();
        }
        if (cause != null) {
            StaleObjectStateException staleObjectStateException = (StaleObjectStateException) cause;
            String entityName = staleObjectStateException.getEntityName();
            Serializable identifier = staleObjectStateException.getIdentifier();
            RepositoryException re = new RepositoryException("Stale object encountered", throwable);
            re.setEntityClassName(entityName);
            re.setEntityIdentifier(identifier.toString());
            String[] names = findEntityName(entityName, identifier);
            re.setEntityName(names[0]);
            re.setEntityAsString(names[1]);
            return re;
        }
        return new RepositoryException("Unknown DataStorage Exception", throwable);
    }

    private String[] findEntityName(String entityClassName, Serializable identifier) {
        String[] result = new String[2];
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            Object entity = entityManager.find(entityClass, identifier);
            if (entity != null) {
                Method getNameMethod = entity.getClass().getMethod("getName");
                Object name = getNameMethod.invoke(entity);
                result[0] = name.toString();
                result[1] = entity.toString();
            }
        } catch (Exception ignore) {
            logger.error("unable to find entity " + entityClassName + "#" + identifier.toString());
        }
        return result;
    }

    private ApplicationProperty loadApplicationProperty(ApplicationProperty applicationProperty) throws RepositoryException {
        ApplicationProperty appProp;
        try {
            appProp = entityManager.find(ApplicationProperty.class, applicationProperty.getId());
        } catch (Exception e) {
            throw new RepositoryException("Loading ApplicationProperty failed.", e);
        }
        return appProp;
    }

    private boolean storeApplicationProperty(ApplicationProperty applicationProperty) throws RepositoryException {
        try {
            entityManager.setFlushMode(FlushModeType.AUTO);
            ApplicationProperty appProp = entityManager.find(ApplicationProperty.class, applicationProperty.getId());
            if (appProp == null) {
                entityManager.persist(applicationProperty);
            } else {
                entityManager.merge(applicationProperty);
            }
            return true;
        } catch (Exception e) {
            throw new RepositoryException("Storing ApplicationProperty failed.", e);
        }
    }

    public String loadSchemeVersion() throws RepositoryException {
        try {
            ApplicationProperty appProp = entityManager.find(ApplicationProperty.class, SCHEME_VERSION_APPLICATION_PROPERTY_ID);
            return appProp.getValue();
        } catch (Exception e) {
            throw new RepositoryException("Loading ApplicationProperty failed.", e);
        }
    }

    public boolean storeSchemeVersion(String schemeVersion) throws RepositoryException {
        try {
            entityManager.setFlushMode(FlushModeType.AUTO);
            ApplicationProperty applicationProperty = new ApplicationProperty();
            applicationProperty.setId(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
            applicationProperty.setName(SCHEME_VERSION_APPLICATION_PROPERTY_NAME);
            applicationProperty.setValue(schemeVersion);
            ApplicationProperty appProp = entityManager.find(ApplicationProperty.class, applicationProperty.getId());
            if (appProp == null) {
                entityManager.persist(applicationProperty);
            } else {
                entityManager.merge(applicationProperty);
            }
            return true;
        } catch (Exception e) {
            throw new RepositoryException("Storing ApplicationProperty failed.", e);
        }
    }

}
