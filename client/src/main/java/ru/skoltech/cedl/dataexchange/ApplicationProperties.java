package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class embodies the properties that are part of the deployed package.
 * <p>
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationProperties {

    private static final Logger logger = Logger.getLogger(ApplicationProperties.class);
    private static final String PROPERTIES_FILE = "/cedesk.properties";

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static String getAppBuildTime() {
        return properties.getProperty("app.build.time", "<>");
    }

    public static String getAppDistributionServerUrl() {
        return properties.getProperty("app.distribution.server.url", "<>");
    }

    public static String getAppVersion() {
        return properties.getProperty("app.version", "<>");
    }

    public static String getDbSchemaVersion() {
        return properties.getProperty("db.schema.version", "<>");
    }

    public static String getDefaultRepositoryHost() {
        return properties.getProperty("default.repository.host", "<>");
    }

    private static synchronized void load() {
        Properties props = new Properties();
        try (InputStream stream =
                     ApplicationProperties.class.getResourceAsStream(PROPERTIES_FILE)) {
            props.load(stream);
            properties = props;
        } catch (IOException e) {
            logger.error("error loading application properties: " + PROPERTIES_FILE, e);
        }
    }
}
