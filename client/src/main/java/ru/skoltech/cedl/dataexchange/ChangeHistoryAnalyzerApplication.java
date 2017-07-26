/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

/**
 * Created by d.knoll on 29.12.2016.
 */
public class ChangeHistoryAnalyzerApplication extends Application {

    private static Logger logger = Logger.getLogger(ChangeHistoryAnalyzerApplication.class);

    private static ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();

    public static void main(String[] args) {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        PropertyConfigurator.configure(ClientApplication.class.getResource("/log4j/log4j.properties"));
        System.out.println("using: " + applicationSettings.getCedeskAppDir() + "/" + applicationSettings.getCedeskAppFile());

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = applicationSettings.getApplicationVersion();
        String dbSchemaVersion = applicationSettings.getRepositorySchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);

        Project project = context.getBean(Project.class);
        String projectName = applicationSettings.getLastUsedProject();
        project.setProjectName(projectName != null ? projectName : "demoSAT"); // TODO: give choice on ui
        project.loadLocalStudy();

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoaderFactory fxmlLoaderFactory = context.getBean(FXMLLoaderFactory.class);
        FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.ANALYSIS_WINDOW);

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Parameter Change Analysis");
        stage.getIcons().add(IconSet.APP_ICON);
        stage.show();
    }


    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK ...");
        try {
            Project project = context.getBean(Project.class);
            project.close();
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }

}