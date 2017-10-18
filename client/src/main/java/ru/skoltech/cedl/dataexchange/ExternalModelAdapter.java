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

import org.apache.commons.beanutils.BeanUtils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Nikolay Groshkov on 04-Oct-17.
 */
public class ExternalModelAdapter extends XmlAdapter<ExcelExternalModel, ExternalModel> {

    @Override
    public ExcelExternalModel marshal(ExternalModel externalModel) throws Exception {
        if (null == externalModel) {
            return null;
        }
        ExcelExternalModel resultExternalModel = new ExcelExternalModel();
        Utils.copyBean(externalModel, resultExternalModel);

        return resultExternalModel;
    }

    @Override
    public ExternalModel unmarshal(ExcelExternalModel adaptedExternalModel) throws Exception {
        if (null == adaptedExternalModel) {
            return null;
        }

        ExternalModel externalModel = (ExternalModel) BeanUtils.cloneBean(adaptedExternalModel);
        externalModel.init();
        return externalModel;
    }

}
