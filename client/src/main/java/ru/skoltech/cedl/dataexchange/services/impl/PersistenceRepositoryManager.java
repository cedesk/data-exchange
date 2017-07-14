package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import ru.skoltech.cedl.dataexchange.ApplicationProperties;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.PersistenceFactory;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.PersistenceRepositoryService;
import ru.skoltech.cedl.dataexchange.services.RepositoryManager;

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
 * Created by n.groshkov on 04-Jul-17.
 */
public class PersistenceRepositoryManager implements RepositoryManager {

    private static final Logger logger = Logger.getLogger(PersistenceRepositoryManager.class);

    private static final String HIBERNATE_TABLE_MAPPING = "hibernate.hbm2ddl.auto";
    private static final String HIBERNATE_TABLE_MAPPING_UPDATE = "update";

    private Map<String, String> jpaProperties;
    private PersistenceFactory persistenceFactory;
    private PersistenceRepositoryService persistenceRepositoryService;

    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    public void setJpaProperties(Map<String, String> jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    public void setPersistenceFactory(PersistenceFactory persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }

    public void setPersistenceRepositoryService(PersistenceRepositoryService persistenceRepositoryService) {
        this.persistenceRepositoryService = persistenceRepositoryService;
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
                    .persistenceUnit("db")
                    .packages("ru.skoltech.cedl.dataexchange")
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            persistenceRepositoryService.setEntityManager(entityManagerFactory.createEntityManager());
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
        if (localContainerEntityManagerFactoryBean != null) {
            localContainerEntityManagerFactoryBean.destroy();
        }
    }

    @Override
    public boolean checkRepositoryConnection(String hostName, String schema, String userName, String password) {
        String url = String.format(ApplicationSettings.DEFAULT_JDBC_URL_PATTERN, hostName, schema);

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

            Map<String, String> jpaProperties = new HashMap<>(this.jpaProperties);
            jpaProperties.put(HIBERNATE_TABLE_MAPPING, HIBERNATE_TABLE_MAPPING_UPDATE);

            EntityManagerFactoryBuilder entityManagerFactoryBuilder = persistenceFactory.createEntityManagerFactoryBuilder(jpaProperties);
            localContainerEntityManagerFactoryBean = entityManagerFactoryBuilder
                    .dataSource(dataSource)
                    .jta(true)
                    .persistenceUnit("db")
                    .packages("ru.skoltech.cedl.dataexchange")
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            entityManager = entityManagerFactory.createEntityManager();
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
        return checkAndStoreSchemeVersion();
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
                    .persistenceUnit("db")
                    .packages("ru.skoltech.cedl.dataexchange")
                    .build();
            localContainerEntityManagerFactoryBean.afterPropertiesSet();

            EntityManagerFactory entityManagerFactory = localContainerEntityManagerFactoryBean.getObject();
            entityManager = entityManagerFactory.createEntityManager();
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
        return checkSchemeVersion();
    }

    private boolean checkAndStoreSchemeVersion() {
        String currentSchemaVersion = ApplicationProperties.getDbSchemaVersion();
        String actualSchemaVersion = null;
        try {
            actualSchemaVersion = persistenceRepositoryService.loadSchemeVersion();
        } catch (RepositoryException e) {
            logger.debug("error loading the applications version property", e);
        }
        if (actualSchemaVersion == null) {
            try {
                return persistenceRepositoryService.storeSchemeVersion(currentSchemaVersion);
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
            return persistenceRepositoryService.storeSchemeVersion(currentSchemaVersion);
        } catch (RepositoryException e) {
            logger.debug("error storing the applications version property", e);
            return false;
        }
    }

    private boolean checkSchemeVersion() {
        String currentSchemaVersion = ApplicationProperties.getDbSchemaVersion();
        String actualSchemaVersion = null;
        try {
            actualSchemaVersion = persistenceRepositoryService.loadSchemeVersion();
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
