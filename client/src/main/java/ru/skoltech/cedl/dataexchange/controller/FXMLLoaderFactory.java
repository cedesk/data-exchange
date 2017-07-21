/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

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
     * Passed <i>*.fxml</i> file contains record about controler {@link Class}.
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
}
