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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.controller.FXMLLoaderFactory;
import ru.skoltech.cedl.dataexchange.controller.TradespaceController;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.services.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.tradespace.TradespaceFactory;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.net.URL;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceExplorerApplication extends ContextAwareApplication {

    private static Logger logger = Logger.getLogger(TradespaceExplorerApplication.class);

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
    public void start(Stage primaryStage) throws Exception {
        setupContext();
        loadLastProject();

        FXMLLoaderFactory fxmlLoaderFactory = context.getBean(FXMLLoaderFactory.class);
        FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.TRADESPACE_WINDOW);
        Parent root = loader.load();
        TradespaceController tradespaceController = loader.getController();

        URL url = TradespaceExplorerApplication.class.getResource("/GPUdataset_2015.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.readValuesForEpochFromCSV(file, 2015);
        tradespaceController.setModel(multitemporalTradespace);

        primaryStage.setTitle("CEDESK Tradespace Explorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();
        /*primaryStage.setOnCloseRequest(we -> {
            if (!tradespaceController.confirmCloseRequest()) {
                we.consume();
            }
        });*/
    }

}
