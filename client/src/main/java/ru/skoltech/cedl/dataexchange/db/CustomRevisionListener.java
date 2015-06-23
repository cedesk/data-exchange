package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;

import java.io.Serializable;

/**
 * Created by D.Knoll on 22.06.2015.
 */
public class CustomRevisionListener implements EntityTrackingRevisionListener {

    @Override
    public void entityChanged(Class entityClass, String entityName,
                              Serializable entityId, RevisionType revisionType,
                              Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        revision.setUsername(ApplicationSettings.getLastUsedUser()); // not certainly always carries the "current" value
    }

    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        revision.setUsername(ApplicationSettings.getLastUsedUser()); // not certainly always carries the "current" value
    }
}