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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.db.RepositoryException;
import ru.skoltech.cedl.dataexchange.entity.ApplicationProperty;
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

    @Autowired
    public RepositorySchemeServiceImpl(ApplicationPropertyRepository applicationPropertyRepository) {
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

    @Override
    public void checkSchemeVersion(String version) throws RepositoryException {
        ApplicationProperty schemeVersionProperty = applicationPropertyRepository.findOne(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
        if (schemeVersionProperty == null) {
            throw new RepositoryException("No DB Schema Version!\nHave the administrator setup version of the DB Schema!");
        }

        String actualSchemaVersion = schemeVersionProperty.getValue();

        int versionCompare;
        try {
            versionCompare = Utils.compareVersions(actualSchemaVersion, version);
        } catch (Exception e) {
            throw new RepositoryException("DB Schema Version is not valid: " + actualSchemaVersion
                    + "\nHave the administrator setup correct version the DB Schema!");
        }

        if (versionCompare < 0) {
            throw new RepositoryException("Have the administrator upgrade the DB Schema!\n"
                    + "Current Application Version requires a DB Schema Version " + version + ", "
                    + "which is incompatible with current DB Schema Version: " + actualSchemaVersion);
        }
        if (versionCompare > 0) {
            throw new RepositoryException("Upgrade your CEDESK Client!\n"
                    + "Current Application Version requires a DB Schema Version " + version + ", "
                    + "which is incompatible with current DB Schema Version: " + actualSchemaVersion);
        }
    }

    @Override
    public void storeSchemeVersion(String version) throws RepositoryException {
        try {
            ApplicationProperty applicationProperty = new ApplicationProperty();
            applicationProperty.setId(SCHEME_VERSION_APPLICATION_PROPERTY_ID);
            applicationProperty.setName(SCHEME_VERSION_APPLICATION_PROPERTY_NAME);
            applicationProperty.setValue(version);

            applicationPropertyRepository.saveAndFlush(applicationProperty);
        } catch (Exception e) {
            throw new RepositoryException("Cannot upgrade repository schema version to the version: " + version, e);
        }
    }
}
