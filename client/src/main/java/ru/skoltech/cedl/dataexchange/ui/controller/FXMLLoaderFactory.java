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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.fxml.FXMLLoader;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URL;

/**
 * Factory for creation {@link FXMLLoader}s which are based on context controllers.
 *
 * Created by Nikolay Groshkov on 19-Jul-17.
 */
public class FXMLLoaderFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Create {@link FXMLLoader} instance.
     * Passed <i>*.fxml</i> file contains record about controller {@link Class}.
     * By use of this record required controller bean is retrieved from application context.
     *
     * @param location path to the <i>*.fxml</i> resource
     * @return FXMLLoader instance
     */
    public FXMLLoader createFXMLLoader(URL location) {
        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(clazz -> applicationContext.getBean(clazz));
        return loader;
    }

    /**
     * Create {@link FXMLLoader} instance.
     * Passed <i>*.fxml</i> file contains record about controller {@link Class}.
     * By use of this record required controller bean is retrieved from application context,
     * where it must be stored under prototype scope.
     * Passed arguments use for instantiating.
     *
     * @param location path to the <i>*.fxml</i> resource
     * @param args arguments for controller instantiating
     * @return FXMLLoader instance
     */
    public FXMLLoader createFXMLLoader(URL location, Object... args) {
        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(clazz -> applicationContext.getBean(clazz, args));
        return loader;
    }

}
