package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
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
    private long flushTimeStamp;

    @Override
    public void afterTransactionBegin(Transaction tx) {
        flushTimeStamp = System.currentTimeMillis(); // FIX: problem when clocks among clients are unsyncronized 
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
                                Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

        if (entity instanceof ModificationTimestamped) {
            logger.debug(entity.getClass().getCanonicalName() + "#" + id + ":" + getNodeName(currentState, propertyNames));
            ModificationTimestamped timestampedEntity = (ModificationTimestamped) entity;
            timestampedEntity.setLastModification(flushTimeStamp);
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id,
                          Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof ModificationTimestamped) {
            String nodeName = null;
            if (entity instanceof ModelNode) {
                nodeName = ((ModelNode) entity).getNodePath();
            } else if (entity instanceof ParameterModel) {
                nodeName = ((ParameterModel) entity).getNodePath();
            } else if (entity instanceof ExternalModel) {
                nodeName = ((ExternalModel) entity).getNodePath();
            } else {
                nodeName = getNodeName(state, propertyNames);
            }
            logger.debug(entity.getClass().getCanonicalName() + "#" + id + ":" + nodeName);
            ModificationTimestamped timestampedEntity = (ModificationTimestamped) entity;
            timestampedEntity.setLastModification(flushTimeStamp);
        }
        return false;
    }

    private String getNodeName(Object[] state, String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals("name")) {
                return (String) state[i];
            }
        }
        return null;
    }
}