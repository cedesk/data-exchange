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

/**
 * Created by d.knoll on 14.11.2016.
 */

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import ru.skoltech.cedl.dataexchange.StaticSystemBuilder;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextDemo;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.io.File;
import java.io.IOException;

public class DsmControllerDemo extends AbstractApplicationContextDemo {

    private static String[] launchArguments;

    public static void main(String[] args) {
        launchArguments = args;
        Application.launch(args);
    }

    @Override
    public void demo(Stage stage) {

        Project project = context.getBean(Project.class);
        FileStorageService fileStorageService = context.getBean(FileStorageService.class);

        SystemModel systemModel = null;
        if (launchArguments.length == 1) {
            File importFile = new File(launchArguments[0]);
            String fileExtension = FilenameUtils.getExtension(importFile.getName());
            Study study = null;
            try {
                study = fileStorageService.importStudyFromZip(importFile);
                project.importStudy(study);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            systemModel = StaticSystemBuilder.makeCarWith4Subsystems();
            project.importSystemModel(systemModel);
        }

        GuiService guiService = context.getBean(GuiService.class);
        ViewBuilder viewBuilder = guiService.createViewBuilder("DSM Demo", Views.DSM_VIEW);
        viewBuilder.showAndWait();

    }

}