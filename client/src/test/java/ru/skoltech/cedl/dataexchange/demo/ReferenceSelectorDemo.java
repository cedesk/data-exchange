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

import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextDemo;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by D.Knoll on 25.09.2015.
 */
public class ReferenceSelectorDemo extends AbstractApplicationContextDemo {

    private static Logger logger = Logger.getLogger(ReferenceSelectorDemo.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void demo(Stage primaryStage) {
        try {
            GuiService guiService = context.getBean(GuiService.class);

            ParameterModel parameterModel = getParameterModel();

            System.out.println(parameterModel);
            parameterModel.updateValueReference();
            System.out.println(parameterModel);

            ExternalModel importModel = parameterModel.getImportModel();
            String importField = parameterModel.getImportField();
            List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();

            ViewBuilder referenceSelectorViewBuilder = guiService.createViewBuilder("Reference Selector", Views.REFERENCE_SELECTOR_VIEW);
            referenceSelectorViewBuilder.ownerWindow(primaryStage);
            referenceSelectorViewBuilder.applyEventHandler(event -> {
                @SuppressWarnings("unchecked")
                Pair<ExternalModel, String> externalModelReference = (Pair<ExternalModel, String>) event.getSource();
                System.out.println(externalModelReference != null ?
                        externalModelReference.getLeft().getName() + ":" + externalModelReference.getRight() : "(empty)");
            });
            referenceSelectorViewBuilder.showAndWait(importModel, importField, externalModels);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));

        }
    }

    private ParameterModel getParameterModel() throws IllegalAccessException, NoSuchFieldException {
        Project project = context.getBean(Project.class);
        project.initProject("TEST");
        Study study = new Study("TEST");
        Field field = Project.class.getDeclaredField("study");
        field.setAccessible(true);
        field.set(project, study);

        SystemModel systemModel = new SystemModel("ROOT-SYS");
        study.setSystemModel(systemModel);
        ParameterModel parameterModel = new ParameterModel("param", 123.45);
        systemModel.addParameter(parameterModel);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);

        try {
            ExternalModelService externalModelService = context.getBean(ExternalModelService.class);
            SystemModel testSat = new SystemModel("testSat");
            File file = new File(ReferenceSelectorDemo.class.getResource("/simple-model.xls").toURI());
            ExternalModel externalModel = externalModelService.createExternalModelFromFile(file, testSat);
            systemModel.addExternalModel(externalModel);
            parameterModel.setImportModel(externalModel);
            parameterModel.setImportField("G4");

            file = new File(ReferenceSelectorDemo.class.getResource("/attachment.xls").toURI());
            externalModel = externalModelService.createExternalModelFromFile(file, testSat);
            systemModel.addExternalModel(externalModel);

            file = new File(ReferenceSelectorDemo.class.getResource("/attachment.csv").toURI());
            externalModel = externalModelService.createExternalModelFromFile(file, testSat);
            systemModel.addExternalModel(externalModel);
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        }
        return parameterModel;
    }
}
