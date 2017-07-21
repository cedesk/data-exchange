package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.external.*;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nikolay Groshkov on 07-Jul-17.
 */
public interface ModelUpdateService {

    /**
     * TODO add javadoc
     *
     * @param project
     * @param externalModel
     * @param externalModelFileHandler
     * @param modelUpdateListeners
     * @param parameterUpdateListener
     * @throws ExternalModelException
     */
    void applyParameterChangesFromExternalModel(Project project, ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler,
                                                List<? extends Consumer<ModelUpdate>> modelUpdateListeners, Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException;

    /**
     * TODO add javadoc
     *
     * @param project
     * @param parameterModel
     * @param externalModelFileHandler
     * @param parameterUpdateListener
     * @throws ExternalModelException
     */
    void applyParameterChangesFromExternalModel(Project project, ParameterModel parameterModel, ExternalModelFileHandler externalModelFileHandler,
                                                Consumer<ParameterUpdate> parameterUpdateListener) throws ExternalModelException;

    /**
     * TODO add javadoc
     *
     * @param project
     * @param externalModel
     * @param externalModelFileHandler
     * @param externalModelFileWatcher
     * @throws ExternalModelException
     */
    void applyParameterChangesToExternalModel(Project project, ExternalModel externalModel, ExternalModelFileHandler externalModelFileHandler, ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException;
}
