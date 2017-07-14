package ru.skoltech.cedl.dataexchange.db;

import org.springframework.beans.BeansException;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Constructs {@link DataSource} object based on the {@link ApplicationSettings} properties.
 * Passed constructed {@link EntityManagerFactoryBuilder} from application context
 * or create a new one based on <i>jpaProperties</i>.
 *
 * Created by D.Knoll on 25.05.2015.
 */
public class ApplicationSettingsPersistenceFactory implements PersistenceFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private JpaVendorAdapter jpaVendorAdapter;
    private EntityManagerFactoryBuilder entityManagerFactoryBuilder;
    private ApplicationSettings applicationSettings;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setJpaVendorAdapter(JpaVendorAdapter jpaVendorAdapter) {
        this.jpaVendorAdapter = jpaVendorAdapter;
    }

    public void setEntityManagerFactoryBuilder(EntityManagerFactoryBuilder entityManagerFactoryBuilder) {
        this.entityManagerFactoryBuilder = entityManagerFactoryBuilder;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public DataSource createDataSource() {
        String url = applicationSettings.getRepositoryUrl(ApplicationSettings.DEFAULT_HOST_NAME, ApplicationSettings.DEFAULT_SCHEMA);
        String user = applicationSettings.getRepositoryUserName(ApplicationSettings.DEFAULT_USER_NAME);
        String password = applicationSettings.getRepositoryPassword(ApplicationSettings.DEFAULT_PASSWORD);

        DriverManagerDataSource driverManagerDataSource = applicationContext.getBean(DriverManagerDataSource.class);
        driverManagerDataSource.setUrl(url);
        driverManagerDataSource.setUsername(user);
        driverManagerDataSource.setPassword(password);

        return driverManagerDataSource;
    }

    @Override
    public EntityManagerFactoryBuilder createEntityManagerFactoryBuilder() {
        return entityManagerFactoryBuilder;
    }

    @Override
    public EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(Map<String, ?> jpaProperties) {
        return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties, null);
    }
}