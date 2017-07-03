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
    private static final String DEFAULT_CONTEXT_PATH = "/META-INF/context.xml";

    private static String path;
    private static ApplicationContextInitializer instance;

    /**
     * Provide a special path to the application context.
     * For testing purposes.
     *
     * @param contextPath path to application context;
     */
    static void initialize(String contextPath) {
        if (path != null) {
            logger.warn("Application context has already been initialized.");
            return;
        }
        path = contextPath;
    }

    public static ApplicationContextInitializer getInstance() {
        if (instance == null) {
            if (path == null) {
                path = DEFAULT_CONTEXT_PATH;
            }
            instance = new ApplicationContextInitializer(path);
        }
        return instance;
    }


    private final ApplicationContext context;

    private ApplicationContextInitializer(String contextPath) {
        context = new ClassPathXmlApplicationContext(new String[] {contextPath});
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