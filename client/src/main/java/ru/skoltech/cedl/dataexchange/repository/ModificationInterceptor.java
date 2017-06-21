package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModificationTimestamped;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

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
            Long timestamp = null;
            if (!(entity instanceof ExternalModel)) {
                timestamp = updateTimestamp(currentState, propertyNames);
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
            Long timestamp = null;
            if (!(entity instanceof ExternalModel)) {
                timestamp = updateTimestamp(currentState, propertyNames);
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
                long latestModification = study.getSystemModel().findLatestModification();
                study.setLatestModelModification(latestModification);
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