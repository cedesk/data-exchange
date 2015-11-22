package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;

import java.io.Serializable;

/**
 * Created by D.Knoll on 22.06.2015.
 */
public class CustomRevisionListener implements EntityTrackingRevisionListener {

    @Override
    public void entityChanged(Class entityClass, String entityName,
                              Serializable entityId, RevisionType revisionType,
                              Object revisionEntity) {
        // empty
    }

    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        revision.setUsername(ApplicationSettings.getProjectUser()); // not certainly always carries the "current" value
    }
}