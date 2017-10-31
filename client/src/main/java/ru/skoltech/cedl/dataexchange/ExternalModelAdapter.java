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

import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.CsvExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * Created by Nikolay Groshkov on 04-Oct-17.
 */
public class ExternalModelAdapter extends XmlAdapter<ExternalModelAdapter.AdaptedExternalModel, ExternalModel> {

    private static final String EXCEL_TYPE = "EXCEL";
    private static final String CSV_TYPE = "CSV";
    private static final String DEFAULT_TYPE = EXCEL_TYPE;

    @Override
    public AdaptedExternalModel marshal(ExternalModel externalModel) throws Exception {
        if (null == externalModel) {
            return null;
        }
        String type;
        if (externalModel instanceof ExcelExternalModel) {
            type = EXCEL_TYPE;
        } else if (externalModel instanceof CsvExternalModel) {
            type = CSV_TYPE;
        } else {
            type = DEFAULT_TYPE;
        }
        AdaptedExternalModel adaptedExternalModel = new AdaptedExternalModel();
        adaptedExternalModel.setType(type);
        Utils.copyBean(externalModel, adaptedExternalModel);

        return adaptedExternalModel;
    }

    @Override
    public ExternalModel unmarshal(AdaptedExternalModel adaptedExternalModel) throws Exception {
        if (null == adaptedExternalModel) {
            return null;
        }

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

    public static class AdaptedExternalModel extends ExternalModel {

        private String type = DEFAULT_TYPE;

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public Double getValue(String target) throws ExternalModelException {
            return null;
        }

        @Override
        public List<Double> getValues(List<String> targets) throws ExternalModelException {
            return null;
        }

        @Override
        public void setValue(String target, Double value) throws ExternalModelException {

        }

        @Override
        public void setValues(List<Pair<String, Double>> values) throws ExternalModelException {

        }
    }

}
