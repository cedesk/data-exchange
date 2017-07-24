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

package ru.skoltech.cedl.dataexchange.logging;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.services.RepositoryService;

/**
 * Created by D.Knoll on 08.12.2015.
 */
public class ActionLogger {

    private static final Logger logger = Logger.getLogger(ActionLogger.class);

    private ApplicationSettings applicationSettings;
    private RepositoryService repositoryService;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void log(ActionType actionType, String description) {
        log(actionType.name(), description);
    }

    public void log(String action, String description) {
        String user = applicationSettings.getProjectUser();
        LogEntry logEntry = buildEntry(user, action, description);
        logger.info(logEntry.toString());
        if (repositoryService == null) {
            logger.error("Unable to store log in repository.");
        }
        repositoryService.storeLog(logEntry);
    }

    private static LogEntry buildEntry(String user, String action, String description) {
        String client = Utils.getFullHostname();
        LogEntry logEntry = new LogEntry(user, client, action, description);
        return logEntry;
    }

    public enum ActionType {
        APPLICATION_START,
        APPLICATION_STOP,
        PROJECT_NEW,
        PROJECT_LOAD,
        PROJECT_SAVE,
        PROJECT_DELETE,
        PROJECT_EXPORT,
        PROJECT_IMPORT,
        USER_VALIDATE,
        NODE_ADD,
        NODE_REMOVE,
        PARAMETER_ADD,
        PARAMETER_REMOVE,
        PARAMETER_MERGE,
        PARAMETER_MODIFY_MANUAL,
        PARAMETER_MODIFY_LINK,
        PARAMETER_MODIFY_REFERENCE,
        EXTERNAL_MODEL_ADD,
        EXTERNAL_MODEL_REMOVE,
        EXTERNAL_MODEL_MERGE,
        EXTERNAL_MODEL_MODIFY
    }
}
