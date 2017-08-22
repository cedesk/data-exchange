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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ApplicationProperty;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.jpa.ApplicationPropertyRepository;
import ru.skoltech.cedl.dataexchange.service.RepositorySchemeService;

import static ru.skoltech.cedl.dataexchange.repository.jpa.ApplicationPropertyRepository.SCHEME_VERSION_APPLICATION_PROPERTY_ID;
import static ru.skoltech.cedl.dataexchange.repository.jpa.ApplicationPropertyRepository.SCHEME_VERSION_APPLICATION_PROPERTY_NAME;

/**
 * Implementation of {@link RepositorySchemeService}.
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public class RepositorySchemeServiceImpl implements RepositorySchemeService {

    private static final Logger logger = Logger.getLogger(RepositorySchemeServiceImpl.class);
    private final ApplicationPropertyRepository applicationPropertyRepository;
    private ApplicationSettings applicationSettings;

    @Autowired
    public RepositorySchemeServiceImpl(ApplicationPropertyRepository applicationPropertyRepository) {
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public boolean checkAndStoreSchemeVersion() {
        String currentSchemaVersion = applicationSettings.getRepositorySchemaVersion();

        ApplicationProperty schemeVersionProperty = applicationPropertyRepository.findOne(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
        if (schemeVersionProperty == null) {
            logger.warn("No DB Schema Version!");
            return this.saveRepositoryVersion(currentSchemaVersion);
        }

        String actualSchemaVersion = schemeVersionProperty.getValue();

        if (Utils.compareVersions(actualSchemaVersion, currentSchemaVersion) > 0) {
            StatusLogger.getInstance().log("Downgrade your CEDESK Client! "
                    + "Current Application Version (" + currentSchemaVersion + ") "
                    + "is older than current DB Schema Version " + actualSchemaVersion);
            return false;
        }

        return this.saveRepositoryVersion(currentSchemaVersion);
    }

    @Override
    public boolean checkSchemeVersion() {
        String currentSchemaVersion = applicationSettings.getRepositorySchemaVersion();

        ApplicationProperty schemeVersionProperty = applicationPropertyRepository.findOne(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
        if (schemeVersionProperty == null) {
            logger.warn("No DB Schema Version!");
            return false;
        }

        String actualSchemaVersion = schemeVersionProperty.getValue();
        int versionCompare = Utils.compareVersions(actualSchemaVersion, currentSchemaVersion);

        if (versionCompare < 0) {
            StatusLogger.getInstance().log("Upgrade your CEDESK Client! "
                    + "Current Application Version requires a DB Schema Version " + currentSchemaVersion + ", "
                    + "which is incompatible with current DB Schema Version " + actualSchemaVersion);
            return false;
        }
        if (versionCompare > 0) {
            StatusLogger.getInstance().log("Have the administrator upgrade the DB Schema! "
                    + "Current Application Version requires a DB Schema Version " + currentSchemaVersion + ", "
                    + "which is incompatible with current DB Schema Version " + actualSchemaVersion);
            return false;
        }
        return true;
    }

    private boolean saveRepositoryVersion(String schemaVersion) {
        try {
            ApplicationProperty applicationProperty = new ApplicationProperty();
            applicationProperty.setId(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
            applicationProperty.setName(SCHEME_VERSION_APPLICATION_PROPERTY_NAME);
            applicationProperty.setValue(schemaVersion);

            applicationPropertyRepository.saveAndFlush(applicationProperty);
            return true;
        } catch (Exception e) {
            logger.debug("error storing the applications version property", e);
            return false;
        }
    }
}
