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

package ru.skoltech.cedl.dataexchange.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.skoltech.cedl.dataexchange.controller.TradespaceController;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.init.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.SimpleSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.tradespace.TradespaceFactory;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * Created by d.knoll on 6/23/2017.
 */
public class TradespaceWindowDemo extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupContext();

        URL url = TradespaceWindowDemo.class.getResource("/GPUdataset_2015.csv");
        File file = new File(url.getFile());
        MultitemporalTradespace multitemporalTradespace = TradespaceFactory.readValuesForEpochFromCSV(file, 2015);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.TRADESPACE_WINDOW);
        Parent root = loader.load();
        TradespaceController controller = loader.getController();
        controller.setModel(multitemporalTradespace);

        primaryStage.setTitle("Tradespace Window Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();

        closeContext();
    }

    private void closeContext() {
        try {
            ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
            Project project = context.getBean(Project.class);
            context.getBean(ThreadPoolTaskScheduler.class).shutdown();
            context.getBean(ThreadPoolTaskExecutor.class).shutdown();
            project.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void setupContext() {
        try {
            ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
            Project project = context.getBean(Project.class);
            UnitManagementService unitManagementService = context.getBean(UnitManagementService.class);
            Study study = new Study("TS Demo");
            Field sField = Project.class.getDeclaredField("study");
            sField.setAccessible(true);
            sField.set(project, study);

            SimpleSystemBuilder simpleSystemBuilder = new SimpleSystemBuilder();
            simpleSystemBuilder.setUnitManagementService(unitManagementService);
            SystemModel systemModel = simpleSystemBuilder.build(study.getName());
            study.setSystemModel(systemModel);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
