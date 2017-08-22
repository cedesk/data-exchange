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

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

import static ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializerTest.*;

/**
 * Abstract extension of JavaFX {@link Application} which loads an test application context.
 * <p>
 * Created by Nikolay Groshkov on 14-Aug-17.
 */
public abstract class AbstractApplicationContextDemo extends Application {

    protected ConfigurableApplicationContext context;
    private File cedeskAppDir;
    private File cedeskAppFile;

    /**
     * Method which is called after test application context is ready.
     *
     * @param primaryStage the primary stage for this application
     */
    public abstract void demo(Stage primaryStage);

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("user.home", new File("target").getAbsolutePath());

        cedeskAppDir = createCedeskAppDir();
        cedeskAppFile = createCedeskAppFile(cedeskAppDir);

        ApplicationSettingsInitializer.initialize();

        ApplicationContextInitializer.initialize("/context-test.xml");
        context = ApplicationContextInitializer.getInstance().getContext();

        this.demo(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        deleteApplicationSettings(cedeskAppDir, cedeskAppFile);
        context.close();
    }
}
