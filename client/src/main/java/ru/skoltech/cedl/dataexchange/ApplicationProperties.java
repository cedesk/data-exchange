package ru.skoltech.cedl.dataexchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by D.Knoll on 18.03.2015.
 */
public class ApplicationProperties {

    private static Properties properties = new Properties();

    static {
        load();
    }

    public static String getVersion() {
        return properties.getProperty("version", "<>");
    }

    private static synchronized void load() {
        Properties props = new Properties();
        try (InputStream stream =
                     ApplicationProperties.class.getResourceAsStream("/cedesk.properties")) {
            props.load(stream);
            properties = props;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}