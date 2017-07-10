package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Singleton which holds application context and expose it through application.
 *
 * Created by n.groshkov on 03-Jul-17.
 */
public class ApplicationContextInitializer {

    private static Logger logger = Logger.getLogger(ApplicationContextInitializer.class);
    private static final String DEFAULT_CONTEXT_MODEL_LOCATION = "/context-model.xml";
    private static final String DEFAULT_CONTEXT_SERVICE_LOCATION = "/context-service.xml";
    private static final String DEFAULT_CONTEXT_CONTROLLER_LOCATION = "/context-controller.xml";

    private static String[] locations;
    private static ApplicationContextInitializer instance;

    /**
     * Provide a special path to the application context.
     * For testing purposes.
     *
     * @param configLocations locations to application context;
     */
    static void initialize(String[] configLocations) {
        if (locations != null) {
            logger.warn("Application context has already been initialized.");
            return;
        }
        locations = configLocations;
    }

    public static ApplicationContextInitializer getInstance() {
        if (instance == null) {
            if (locations == null) {
                locations = new String[] {DEFAULT_CONTEXT_MODEL_LOCATION,
                                          DEFAULT_CONTEXT_SERVICE_LOCATION,
                                          DEFAULT_CONTEXT_CONTROLLER_LOCATION};
            }
            instance = new ApplicationContextInitializer(locations);
        }
        return instance;
    }

    private final ApplicationContext context;

    private ApplicationContextInitializer(String[] configLocations) {
        context = new ClassPathXmlApplicationContext(configLocations);
    }

    /**
     * Retrieve an application context.
     *
     * @return application context.
     */
    public ApplicationContext getContext() {
        return context;
    }
}