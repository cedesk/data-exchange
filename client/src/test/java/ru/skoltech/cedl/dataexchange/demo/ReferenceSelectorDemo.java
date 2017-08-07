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
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.control.ReferenceSelector;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ParameterUpdate;
import ru.skoltech.cedl.dataexchange.services.ModelUpdateService;
import ru.skoltech.cedl.dataexchange.services.impl.ModelUpdateServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by D.Knoll on 25.09.2015.
 */
public class ReferenceSelectorDemo extends Application {

    private static Logger logger = Logger.getLogger(ReferenceSelectorDemo.class);

    private static Project project;
    private static ModelUpdateService modelUpdateService;

    public static ParameterModel getParameterModel() throws IllegalAccessException, NoSuchFieldException {
        project = new Project();
        project.init("TEST");
        modelUpdateService = new ModelUpdateServiceImpl();
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
            SystemModel testSat = new SystemModel("testSat");
            File file = new File(ReferenceSelectorDemo.class.getResource("/simple-model.xls").toURI());
            ExternalModel externalModel = ExternalModelFileHandler.newFromFile(file, testSat);
            systemModel.addExternalModel(externalModel);
            parameterModel.setValueReference(new ExternalModelReference(externalModel, "G4"));

            file = new File(ReferenceSelectorDemo.class.getResource("/attachment.xls").toURI());
            externalModel = ExternalModelFileHandler.newFromFile(file, testSat);
            systemModel.addExternalModel(externalModel);
        } catch (Exception e) {
            logger.error(e);
            System.exit(-1);
        }
        return parameterModel;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        ParameterModel parameterModel = getParameterModel();
        ExternalModelFileHandler externalModelFileHandler = project.getExternalModelFileHandler();

        System.out.println(parameterModel);
        modelUpdateService.applyParameterChangesFromExternalModel(project, parameterModel, externalModelFileHandler,
                new Consumer<ParameterUpdate>() {
                    @Override
                    public void accept(ParameterUpdate parameterUpdate) {
                        System.out.println(parameterUpdate);
                    }
                });
        ExternalModelReference valueReference = parameterModel.getValueReference();
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(project, valueReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference externalModelReference = referenceOptional.get();
            System.out.println(externalModelReference);
        }
        primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        System.exit(1);
    }
}
