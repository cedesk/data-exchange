/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.controller.FXMLLoaderFactory;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

/**
 * Created by d.knoll on 29.12.2016.
 */
public class ChangeHistoryAnalyzerApplication extends ContextAwareApplication {

    private static Logger logger = Logger.getLogger(ChangeHistoryAnalyzerApplication.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure(TradespaceExplorerApplication.class.getResource("/log4j/log4j.properties"));

        ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        FileStorageService fileStorageService = context.getBean(FileStorageService.class);
        System.out.println("using: " + fileStorageService.applicationDirectory().getAbsolutePath() +
                "/" + applicationSettings.getCedeskAppFile());

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setupContext();
        loadLastProject();

        FXMLLoaderFactory fxmlLoaderFactory = context.getBean(FXMLLoaderFactory.class);
        FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.ANALYSIS_WINDOW);

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("Parameter Change Analysis");
        stage.getIcons().add(IconSet.APP_ICON);
        stage.show();
    }

}