package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ClientApplication;

import java.io.Serializable;

/**
 * Record the username who initiated a new revision
 *
 * Created by D.Knoll on 22.06.2015.
 */
public class CustomRevisionListener implements EntityTrackingRevisionListener {

    @Override
    public void entityChanged(Class entityClass, String entityName,
                              Serializable entityId, RevisionType revisionType,
                              Object revisionEntity) {
        // empty
    }

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        // TODO: rewrite for proper DI use, maybe try spring-data-envers
        ApplicationSettings applicationSettings = ClientApplication.context.getBean("applicationSettings", ApplicationSettings.class);
        revision.setUsername(applicationSettings.getProjectUser()); // not certainly always carries the "current" value
    }
}