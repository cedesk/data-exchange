package ru.skoltech.cedl.dataexchange.logging;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.repository.Repository;

/**
 * Created by D.Knoll on 08.12.2015.
 */
public class ActionLogger {

    private static final Logger logger = Logger.getLogger(ActionLogger.class);

    private ApplicationSettings applicationSettings;

    public ActionLogger(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void log(ActionType actionType, String description) {
        log(actionType.name(), description);
    }

    public void log(String action, String description) {
        Repository repository = ProjectContext.getInstance().getProject().getRepository();
        LogEntry logEntry = buildEntry(action, description);
        logger.info(logEntry.toString());
        repository.storeLog(logEntry);
    }

    private LogEntry buildEntry(String action, String description) {
        String user = applicationSettings.getProjectUser();
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
