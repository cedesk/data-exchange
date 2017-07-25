/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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
