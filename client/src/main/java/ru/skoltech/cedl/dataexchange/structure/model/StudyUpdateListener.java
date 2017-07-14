package ru.skoltech.cedl.dataexchange.structure.model;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;

import java.util.Map;

/**
 * Listener performs as a trigger on merge event.
 * Sets up actual model version on {@link Study}.
 *
 * Created by n.groshkov on 14-Jul-17.
 */
public class StudyUpdateListener implements MergeEventListener {
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        if (event.getEntity() instanceof Study) {
            Study study = (Study) event.getEntity();
            Long currentModelModification = study.getLatestModelModification() != null ? study.getLatestModelModification() : -1;
            long newModelModification = study.getSystemModel().findLatestModification();
            long latestModelModification = Math.max(currentModelModification + 1, newModelModification);
            study.setLatestModelModification(latestModelModification);
        }
    }

    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        this.onMerge(event);
    }
}
