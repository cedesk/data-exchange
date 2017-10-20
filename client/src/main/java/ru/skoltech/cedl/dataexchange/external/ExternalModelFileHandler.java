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

package ru.skoltech.cedl.dataexchange.external;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelTreeIterator;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by dknoll on 02/07/15.
 */
public class ExternalModelFileHandler {

    private static Logger logger = Logger.getLogger(ExternalModelFileHandler.class);

    private Set<ExternalModel> changedExternalModels = new HashSet<>();

    public void initializeStateOfExternalModels(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        changedExternalModels.clear();
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();

            // keep track of changed files
            switch (externalModel.state()) {
                case CACHE_MODIFIED:
                    changedExternalModels.add(externalModel);
                    logger.debug(externalModel.getParent().getNodePath() + " external model '" + externalModel.getName() + "' has been changed since last store to repository");
                    break;
                case CACHE_CONFLICT:
                    logger.warn(externalModel.getParent().getNodePath() + " external model '" + externalModel.getName() + "' has conflicting changes locally and in repository");
            }
        }
    }

//    public void addChangedExternalModel(ExternalModel externalModel) {
//        changedExternalModels.add(externalModel);
//    }
//
//    /**
//     * check the locally cached external model files for modifications,
//     * and if there are modifications, update the local study model in memory.
//     */
//    public void updateExternalModelsAttachment() {
//        try {
//            for (ExternalModel externalModel : changedExternalModels) {
//                externalModel.updateAttachment();
//            }
//            changedExternalModels.clear();
//        } catch (IOException e) {
//            logger.error("Cannot update external model attachment: " + e.getMessage(), e);
//        }
//    }

//    /**
//     * make sure external model files in cache get a new timestamp
//     */
//    public void updateExternalModelTimestamp() {
//        try {
//            for (ExternalModel externalModel : changedExternalModels) {
////                externalModel.updateTimestamp();
//            }
//            changedExternalModels.clear();
//        } catch (IOException e) {
//            logger.error("Cannot update external model timestamp: " + e.getMessage(), e);
//        }
//    }

}
