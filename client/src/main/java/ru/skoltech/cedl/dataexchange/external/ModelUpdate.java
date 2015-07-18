package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

/**
 * Created by D.Knoll on 10.07.2015.
 */
public class ModelUpdate {

    private final ExternalModel externalModel;

    public ModelUpdate(ExternalModel externalModel) {
        this.externalModel = externalModel;
    }

    public ExternalModel getExternalModel() {
        return externalModel;
    }
}
