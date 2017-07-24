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

package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModificationTimestamped;
import ru.skoltech.cedl.dataexchange.structure.model.Study;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by D.Knoll on 25.06.2015.
 */
public class ModificationInterceptor extends EmptyInterceptor {

    private static Logger logger = Logger.getLogger(ModificationInterceptor.class);

    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
                                Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof ModificationTimestamped) {
            if (!(entity instanceof ExternalModel)) {
                Long timestamp = updateTimestamp(currentState, propertyNames);
                ((ModificationTimestamped) entity).setLastModification(timestamp);
                if (logger.isDebugEnabled()) {
                    logger.debug("onFlushDirty " + entity.getClass().getCanonicalName() + "#" + id
                            + ":" + getNodeName(currentState, propertyNames) + ", ts: " + timestamp);
                }
                return true;
            } else if (logger.isDebugEnabled()) {
                logger.debug("onFlushDirty Ext.Mod." + entity.getClass().getCanonicalName() + "#" + id
                        + ":" + getNodeName(currentState, propertyNames));
            }
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] currentState, String[] propertyNames, Type[] types) {
        if (entity instanceof ModificationTimestamped) {
            if (!(entity instanceof ExternalModel)) {
                Long timestamp = updateTimestamp(currentState, propertyNames);
                ((ModificationTimestamped) entity).setLastModification(timestamp);
                if (logger.isDebugEnabled()) {
                    logger.debug("onSave " + entity.getClass().getCanonicalName() + "#" + id
                            + ":" + getNodeName(currentState, propertyNames) + ", ts: " + timestamp);
                }
                return true;
            } else if (logger.isDebugEnabled()) {
                logger.debug("onSave Ext.Mod." + entity.getClass().getCanonicalName() + "#" + id
                        + ":" + getNodeName(currentState, propertyNames));
            }
        }
        return false;
    }

    @Override
    public void preFlush(Iterator entities) {
        for (; entities.hasNext(); ) {
            Object element = entities.next();
            if (element instanceof Study) {
                Study study = (Study) element;
                long currentModelModification = study.getLatestModelModification() != null ? study.getLatestModelModification(): 0;
                long newModelModification = study.getSystemModel().findLatestModification();
                long latestModelModification = Math.max(currentModelModification + 1, newModelModification);
                study.setLatestModelModification(latestModelModification);
                return;
            }
        }
    }

    private String getNodeName(Object[] currentState, String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("name")) {
                return (String) currentState[i];
            }
        }
        return null; // better throw exception
    }

    private Long updateTimestamp(Object[] currentState, String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("lastModification")) {
                Long timestamp = System.currentTimeMillis();
                currentState[i] = timestamp;
                return timestamp;
            }
        }
        return 0L; // better throw exception
    }
}