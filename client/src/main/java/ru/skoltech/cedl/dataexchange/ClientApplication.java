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
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettingsInitializer;
import ru.skoltech.cedl.dataexchange.service.*;
import ru.skoltech.cedl.dataexchange.ui.Views;
import ru.skoltech.cedl.dataexchange.ui.control.ErrorAlert;

import java.io.IOException;
import java.util.Locale;

public class ClientApplication extends Application {

    private static Logger logger = Logger.getLogger(ClientApplication.class);

    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            ApplicationSettingsInitializer.initialize();
            PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));

            context = new ClassPathXmlApplicationContext(ApplicationContextInitializer.BASE_CONTEXT_LOCATION);
            ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
            RepositoryConnectionService repositoryConnectionService = context.getBean(RepositoryConnectionService.class);

            String host = applicationSettings.getRepositoryHost();
            String schemaName = applicationSettings.getRepositorySchemaName();
            String user = applicationSettings.getRepositoryUser();
            String password = applicationSettings.getRepositoryPassword();

            boolean connected = repositoryConnectionService.checkRepositoryConnection(host, schemaName, user, password);
            if (!connected) {
                this.startRepositorySettingsController(primaryStage);
                return;
            }
            context.close();

            context = ApplicationContextInitializer.getInstance().getContext();

            this.startMainController(primaryStage);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            Alert errorAlert = new ErrorAlert(e);
            errorAlert.showAndWait();
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK ...");
        try {
            context.close();
        } catch (Exception e) {
            logger.warn("Cannot stop context correctly: " + e.getMessage(), e);
        }
        logger.info("CEDESK stopped.");
    }

    private void startMainController(Stage primaryStage) throws IOException {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        RepositorySchemeService repositorySchemeService = context.getBean(RepositorySchemeService.class);

        String currentSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        try {
            repositorySchemeService.checkSchemeVersion(currentSchemaVersion);
        } catch (RepositoryException e) {
            try {
                if (applicationSettings.isRepositorySchemaCreate()) {
                    repositorySchemeService.storeSchemeVersion(currentSchemaVersion);
                    logger.info("Repository schema was successfully upgraded to the version: " + currentSchemaVersion);
                } else {
                    throw e;
                }
            } catch (RepositoryException re) {
                Alert errorAlert = new ErrorAlert(re.getMessage(), re);
                errorAlert.showAndWait();
                this.startRepositorySettingsController(primaryStage);
                return;
            }
        }

        FileStorageService fileStorageService = context.getBean(FileStorageService.class);
        GuiService guiService = context.getBean(GuiService.class);

        System.out.println("using: " + fileStorageService.applicationDirectory().getAbsolutePath() +
                "/" + applicationSettings.getCedeskAppFile());
        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        Locale locale = context.getBean(Locale.class);
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);
        logger.info("Locale " + locale);

        ViewBuilder mainViewBuilder = guiService.createViewBuilder("Concurrent Engineering Data Exchange Skoltech", Views.MAIN_VIEW);
        mainViewBuilder.primaryStage(primaryStage);
        mainViewBuilder.show();
    }

    private void startRepositorySettingsController(Stage primaryStage) throws IOException {
        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder repositorySettingsViewBuilder = guiService.createViewBuilder("Repository settings", Views.REPOSITORY_SETTINGS_VIEW);
        repositorySettingsViewBuilder.primaryStage(primaryStage);
        repositorySettingsViewBuilder.show();
    }
}
