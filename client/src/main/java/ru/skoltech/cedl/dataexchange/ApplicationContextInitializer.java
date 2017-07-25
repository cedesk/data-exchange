/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Singleton which holds application context and expose it through application.
 *
 * Created by Nikolay Groshkov on 03-Jul-17.
 */
public class ApplicationContextInitializer {

    private static Logger logger = Logger.getLogger(ApplicationContextInitializer.class);
    private static final String BASE_CONTEXT_LOCATION = "/context-controller.xml";

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
                locations = new String[] {BASE_CONTEXT_LOCATION};
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