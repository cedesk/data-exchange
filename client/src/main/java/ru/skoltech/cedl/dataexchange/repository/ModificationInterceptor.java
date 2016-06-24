package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModificationTimestamped;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.io.Serializable;

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
            }
            logger.debug("onFlushDirty " + entity.getClass().getCanonicalName() + "#" + id + ":" + getNodeName(currentState, propertyNames) + ", ts: " + timestamp);
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id,
                          Object[] currentState, String[] propertyNames, Type[] types) {
        if (entity instanceof ModificationTimestamped) {
            String nodeName = null;
            if (entity instanceof ModelNode) {
                nodeName = ((ModelNode) entity).getNodePath();
            } else if (entity instanceof ParameterModel) {
                nodeName = ((ParameterModel) entity).getNodePath();
            } else if (entity instanceof ExternalModel) {
                nodeName = ((ExternalModel) entity).getNodePath();
            } else {
                nodeName = getNodeName(currentState, propertyNames);
            }
            Long timestamp = null;
            if (!(entity instanceof ExternalModel)) {
                timestamp = updateTimestamp(currentState, propertyNames);
            }
            logger.debug("onSave " + entity.getClass().getCanonicalName() + "#" + id + ":" + nodeName + ", ts: " + timestamp);
        }
        return false;
    }

    private String getNodeName(Object[] currentState, String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("name")) {
                return (String) currentState[i];
            }
        }
        return null;
    }

    private Long updateTimestamp(Object[] currentState, String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("lastModification")) {
                Long timestamp = System.currentTimeMillis();
                currentState[i] = timestamp;
                return timestamp;
            }
        }
        return 0L;
    }
}