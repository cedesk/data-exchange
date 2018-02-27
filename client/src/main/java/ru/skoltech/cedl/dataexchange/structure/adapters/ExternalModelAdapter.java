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

package ru.skoltech.cedl.dataexchange.structure.adapters;

import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.CsvExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Objects;

/**
 * {@link ExternalModel} adapter for JAXB marshalling and unmarshalling.
 *
 * Created by Nikolay Groshkov on 04-Oct-17.
 */
public class ExternalModelAdapter extends XmlAdapter<AdaptedExternalModel, ExternalModel> {

    private static final String EXCEL_TYPE = "EXCEL";
    private static final String CSV_TYPE = "CSV";

    @Override
    public AdaptedExternalModel marshal(ExternalModel externalModel) {
        Objects.requireNonNull(externalModel);

        String type = AdaptedExternalModel.DEFAULT_TYPE;
        if (externalModel instanceof ExcelExternalModel) {
            type = EXCEL_TYPE;
        } else if (externalModel instanceof CsvExternalModel) {
            type = CSV_TYPE;
        }
        AdaptedExternalModel adaptedExternalModel = new AdaptedExternalModel();
        adaptedExternalModel.setType(type);
        Utils.copyBean(externalModel, adaptedExternalModel);

        return adaptedExternalModel;
    }

    @Override
    public ExternalModel unmarshal(AdaptedExternalModel adaptedExternalModel) {
        Objects.requireNonNull(adaptedExternalModel);

        ExternalModel externalModel;
        String type = adaptedExternalModel.getType();
        if (EXCEL_TYPE.equals(type)) {
            externalModel = new ExcelExternalModel();
        } else if (CSV_TYPE.equals(type)) {
            externalModel = new CsvExternalModel();
        } else {
            throw new AssertionError("Never must be thrown");
        }

        Utils.copyBean(adaptedExternalModel, externalModel);
        externalModel.init();
        return externalModel;
    }

}
