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

    public static void log(ActionType actionType, String description) {
        log(actionType.name(), description);
    }

    public static void log(String action, String description) {
        Repository repository = ProjectContext.getInstance().getProject().getRepository();
        LogEntry logEntry = buildEntry(action, description);
        logger.info(logEntry.toString());
        repository.storeLog(logEntry);
    }

    private static LogEntry buildEntry(String action, String description) {
        String user = ApplicationSettings.getProjectUser();
        String client = Utils.getFullHostname();
        LogEntry logEntry = new LogEntry(user, client, action, description);
        return logEntry;
    }

    public enum ActionType {
        application_start,
        application_stop,
        project_new,
        project_load,
        project_save,
        project_delete,
        project_export,
        project_import,
        user_validate,
        node_add,
        node_remove,
        parameter_add,
        parameter_remove,
        parameter_merge,
        parameter_modify_manual,
        parameter_modify_link,
        parameter_modify_reference,
        external_model_add,
        external_model_remove,
        external_model_merge,
        external_model_modify
    }
}