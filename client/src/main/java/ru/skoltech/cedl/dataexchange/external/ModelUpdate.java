/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;

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
