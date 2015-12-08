package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.Repository;

/**
 * Created by D.Knoll on 08.12.2015.
 */
public class ActionLogger {

    public static void log(String action, String description) {
        Repository repository = ProjectContext.getInstance().getProject().getRepository();
        LogEntry logEntry = buildEntry(action,description);
        repository.storeLog(logEntry);
    }

    private static LogEntry buildEntry(String action, String description) {
        String user = ApplicationSettings.getProjectUser();
        String client = Utils.getHostname();
        LogEntry logEntry = new LogEntry(user, client, action, description);
        logEntry.setLogTimestamp(System.currentTimeMillis());
        return logEntry;
    }
}
