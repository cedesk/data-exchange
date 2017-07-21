package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.PersistenceFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryServiceMethodInterceptor;
import ru.skoltech.cedl.dataexchange.services.RepositoryManager;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link RepositoryManager} based on the JPA.
 *
 * Created by Nikolay Groshkov on 04-Jul-17.
 */
public class PersistenceRepositoryManager implements RepositoryManager {

    private static final Logger logger = Logger.getLogger(PersistenceRepositoryManager.class);

    private static final String PERSISTENCE_UNIT_NAME = "db";
    private static final String PERSISTENCE_ENTITY_PACKAGE = "ru.skoltech.cedl.dataexchange";
    private static final String HIBERNATE_TABLE_MAPPING = "hibernate.hbm2ddl.auto";
    private static final String HIBERNATE_TABLE_MAPPING_UPDATE = "update";

    private ApplicationSettings applicationSettings;
    private Map<String, Object> jpaProperties;
    private PersistenceFactory persistenceFactory;
    private RepositoryServiceMethodInterceptor repositoryServiceMethodInterceptor;

    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setJpaProperties(Map<String, Object> jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    public void setPersistenceFactory(PersistenceFactory persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }

    public void setRepositoryServiceMethodInterceptor(RepositoryServiceMethodInterceptor repositoryServiceMethodInterceptor) {
        this.repositoryServiceMethodInterceptor = repositoryServiceMethodInterceptor;
    }

    @Override
    public void createRepositoryConnection() throws RepositoryException {
        releaseRepositoryConnection();
        try {
            DataSource dataSource = persistenceFactory.createDataSource();
            EntityManagerFactoryBuilder entityManagerFactoryBuilder = persistenceFactory.createEntityManagerFactoryBuilder();
            localContainerEntityManagerFactoryBean = entityManagerFactoryBuilder
                    .dataSource(dataSource)
                    .jta(true)
                    .persistenceUnit(PERSISTENCE_UNIT_NAME)
                    .packages(PERSISTENCE_ENTITY_PACKAGE)
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            repositoryServiceMethodInterceptor.setEntityManagerFactory(entityManagerFactory);
        } catch (Exception e) {
            logger.fatal("connecting to database failed!", e);
            throw new RepositoryException("database connection failed");
        }
    }

    @Override
    public void releaseRepositoryConnection() {
        destroyLocalContainerEntityManagerFactoryBean(localContainerEntityManagerFactoryBean);
        localContainerEntityManagerFactoryBean = null;
    }

    private void destroyLocalContainerEntityManagerFactoryBean(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        if (localContainerEntityManagerFactoryBean != null && localContainerEntityManagerFactoryBean.getObject() != null) {
            localContainerEntityManagerFactoryBean.destroy();
        }
    }

    @Override
    public boolean checkRepositoryConnection(String hostName, String schema, String userName, String password) {
        String url = persistenceFactory.createRepositoryUrl(hostName, schema);

        logger.debug("repository url: " + url + ", user: " + userName);
        try {
            DriverManager.getConnection(url, userName, password).close();
            logger.info("check of database connection succeeded!");
            return true;
        } catch (SQLException e) {
            logger.warn("check of database connection failed!");
            return false;
        }
    }

    @Override
    public boolean updateRepositoryScheme() {
        logger.info("updating database scheme");
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = null;
        EntityManager entityManager = null;
        try {
            DataSource dataSource = persistenceFactory.createDataSource();

            Map<String, Object> jpaProperties = new HashMap<>(this.jpaProperties);
            jpaProperties.put(HIBERNATE_TABLE_MAPPING, HIBERNATE_TABLE_MAPPING_UPDATE);

            EntityManagerFactoryBuilder entityManagerFactoryBuilder = persistenceFactory.createEntityManagerFactoryBuilder(jpaProperties);
            localContainerEntityManagerFactoryBean = entityManagerFactoryBuilder
                    .dataSource(dataSource)
                    .jta(true)
                    .persistenceUnit(PERSISTENCE_UNIT_NAME)
                    .packages(PERSISTENCE_ENTITY_PACKAGE)
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            entityManager = entityManagerFactory.createEntityManager();
            PersistenceRepositoryServiceImpl persistenceRepositoryService = new PersistenceRepositoryServiceImpl();
            persistenceRepositoryService.setEntityManager(entityManager);
            StatusLogger.getInstance().log("Database scheme updated!");
            return checkAndStoreSchemeVersion(persistenceRepositoryService);
        } catch (Exception e) {
            StatusLogger.getInstance().log("Database scheme update failed!", true);
            logger.error("Database scheme update failed!", e);
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            destroyLocalContainerEntityManagerFactoryBean(localContainerEntityManagerFactoryBean);
        }
    }

    @Override
    public boolean validateRepositoryScheme() {
        logger.info("validating database scheme");
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = null;
        EntityManager entityManager = null;
        try {
            DataSource dataSource = persistenceFactory.createDataSource();

            EntityManagerFactoryBuilder entityManagerFactoryBuilder = persistenceFactory.createEntityManagerFactoryBuilder();
            localContainerEntityManagerFactoryBean = entityManagerFactoryBuilder
                    .dataSource(dataSource)
                    .jta(true)
                    .persistenceUnit(PERSISTENCE_UNIT_NAME)
                    .packages(PERSISTENCE_ENTITY_PACKAGE)
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            entityManager = entityManagerFactory.createEntityManager();
            PersistenceRepositoryServiceImpl persistenceRepositoryService = new PersistenceRepositoryServiceImpl();
            persistenceRepositoryService.setEntityManager(entityManager);
            return checkSchemeVersion(persistenceRepositoryService);
        } catch (Exception e) {
            StatusLogger.getInstance().log("Database scheme validation failed!", true);
            logger.error("Database scheme validation failed!", e);
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            destroyLocalContainerEntityManagerFactoryBean(localContainerEntityManagerFactoryBean);
        }
    }

    private boolean checkAndStoreSchemeVersion(RepositoryService repositoryService) {
        String currentSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        String actualSchemaVersion = null;
        try {
            actualSchemaVersion = repositoryService.loadSchemeVersion();
        } catch (RepositoryException e) {
            logger.debug("error loading the applications version property", e);
        }
        if (actualSchemaVersion == null) {
            try {
                return repositoryService.storeSchemeVersion(currentSchemaVersion);
            } catch (RepositoryException e) {
                logger.debug("error storing the applications version property", e);
                return false;
            }
        }

        if (Utils.compareVersions(actualSchemaVersion, currentSchemaVersion) > 0) {
            StatusLogger.getInstance().log("Downgrade your CEDESK Client! "
                    + "Current Application Version (" + currentSchemaVersion + ") "
                    + "is older than current DB Schema Version " + actualSchemaVersion);
            return false;
        }

        try {
            return repositoryService.storeSchemeVersion(currentSchemaVersion);
        } catch (RepositoryException e) {
            logger.debug("error storing the applications version property", e);
            return false;
        }
    }

    private boolean checkSchemeVersion(RepositoryService repositoryService) {
        String currentSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        String actualSchemaVersion = null;
        try {
            actualSchemaVersion = repositoryService.loadSchemeVersion();
        } catch (RepositoryException e) {
            logger.debug("error loading the applications version property", e);
        }
        if (actualSchemaVersion == null) {
            logger.error("No DB Schema Version!");
            return false;
        }
        int versionCompare = Utils.compareVersions(actualSchemaVersion, currentSchemaVersion);

        if (versionCompare < 0) {
            StatusLogger.getInstance().log("Upgrade your CEDESK Client! "
                    + "Current Application Version requires a DB Schema Version " + currentSchemaVersion + ", "
                    + "which is incompatible with current DB Schema Version " + actualSchemaVersion);
            return false;
        }
        if (versionCompare > 0) {
            StatusLogger.getInstance().log("Have the administrator upgrade the DB Schema! "
                    + "Current Application Version requires a DB Schema Version " + currentSchemaVersion + ", "
                    + "which is incompatible with current DB Schema Version " + actualSchemaVersion);
            return false;
        }
        return true;
    }
}
