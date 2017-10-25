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

package ru.skoltech.cedl.dataexchange.init;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Singleton which holds application context and expose it through application.
 * <p>
 * Created by Nikolay Groshkov on 03-Jul-17.
 */
public class ApplicationContextInitializer {

    public static final String BASE_CONTEXT_LOCATION = "/context-base.xml";
    public static final String SERVICE_CONTEXT_LOCATION = "/context-service.xml";
    public static final String CONTEXT_LOCATION = "/context-controller.xml";
    private static Logger logger = Logger.getLogger(ApplicationContextInitializer.class);
    private static String location;
    private static ApplicationContextInitializer instance;
    private final ConfigurableApplicationContext context;

    private ApplicationContextInitializer(String configLocation) {
        context = new ClassPathXmlApplicationContext(configLocation);
    }

    /**
     * Retrieve an application context.
     *
     * @return application context.
     */
    public ConfigurableApplicationContext getContext() {
        return context;
    }

    public static ApplicationContextInitializer getInstance() {
        if (instance == null) {
            if (location == null) {
                location = CONTEXT_LOCATION;
            }
            instance = new ApplicationContextInitializer(location);
        }
        return instance;
    }

    /**
     * Provide a special path to the application context.
     * For testing purposes.
     *
     * @param configLocation location to application context;
     */
    public static void initialize(String configLocation) {
        if (location != null) {
            logger.warn("Application context has already been initialized.");
            return;
        }
        location = configLocation;
    }
}