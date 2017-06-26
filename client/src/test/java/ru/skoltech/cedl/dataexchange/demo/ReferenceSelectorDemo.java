package ru.skoltech.cedl.dataexchange.demo;

import javafx.application.Application;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.control.ReferenceSelector;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.external.ModelUpdateUtil;
import ru.skoltech.cedl.dataexchange.external.ParameterUpdate;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.*;

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

    public static ParameterModel getParameterModel() throws IllegalAccessException, NoSuchFieldException {
        project = new Project("TEST");
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
        ModelUpdateUtil.applyParameterChangesFromExternalModel(parameterModel, externalModelFileHandler,
                new Consumer<ParameterUpdate>() {
                    @Override
                    public void accept(ParameterUpdate parameterUpdate) {
                        System.out.println(parameterUpdate);
                    }
                });
        ExternalModelReference valueReference = parameterModel.getValueReference();
        List<ExternalModel> externalModels = parameterModel.getParent().getExternalModels();
        Dialog<ExternalModelReference> dialog = new ReferenceSelector(valueReference, externalModels);
        Optional<ExternalModelReference> referenceOptional = dialog.showAndWait();
        if (referenceOptional.isPresent()) {
            ExternalModelReference externalModelReference = referenceOptional.get();
            System.out.println(externalModelReference);
        }
        primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        System.exit(1);
    }
}