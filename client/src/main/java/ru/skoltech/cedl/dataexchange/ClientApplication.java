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

package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.controller.FXMLLoaderFactory;
import ru.skoltech.cedl.dataexchange.controller.MainController;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

public class ClientApplication extends Application {

    private static Logger logger = Logger.getLogger(ClientApplication.class);

    private static ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();

    private MainController mainController;

    public static void main(String[] args) {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        FileStorageService fileStorageService = context.getBean(FileStorageService.class);
        PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));
        System.out.println("using: " + fileStorageService.applicationDirectory().getAbsolutePath() +
                "/" + applicationSettings.getCedeskAppFile());

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoaderFactory fxmlLoaderFactory = context.getBean(FXMLLoaderFactory.class);
        FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.MAIN_WINDOW);

        Parent root = loader.load();
        mainController = loader.getController();
        mainController.checkRepository();
        mainController.checkVersionUpdate();

        primaryStage.setTitle("Concurrent Engineering Data Exchange Skoltech");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();
        primaryStage.setOnCloseRequest(we -> {
            if (!mainController.confirmCloseRequest()) {
                we.consume();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK ...");
        try {
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
            mainController.terminate();
        } catch (Exception e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }
}
