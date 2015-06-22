package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.RevisionListener;

/**
 * Created by D.Knoll on 22.06.2015.
 */
public class CustomRevisionListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        revision.setUsername("username"); //for testing
    }
}