/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.db;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;

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
        ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        revision.setUsername(applicationSettings.getProjectUser()); // not certainly always carries the "current" value
    }
}