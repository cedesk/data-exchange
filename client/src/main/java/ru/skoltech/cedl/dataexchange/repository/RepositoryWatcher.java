package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.sql.Timestamp;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class RepositoryWatcher extends Thread {

    public static final long SECONDS_OF_CHECK_PERIODICITY = 10;

    private static final Logger logger = Logger.getLogger(RepositoryWatcher.class);

    private final Project project;

    private boolean continueRunning = true;

    private Repository repository;

    public RepositoryWatcher(Project project) {
        this.project = project;
        this.repository = RepositoryFactory.getDatabaseRepository();
    }

    @Override
    public void run() {
        try {
            sleep(SECONDS_OF_CHECK_PERIODICITY * 1000);
        } catch (InterruptedException ignore) {
        }
        while (continueRunning) {
            try {
                if (project.getStudy() != null) {
                    // load model from repository
                    long systemModelId = project.getStudy().getSystemModel().getId();
                    SystemModel systemModel = repository.loadSystemModel(systemModelId);
                    Timestamp latestModification = systemModel.findLatestModification();
                    project.latestRepositoryModificationProperty().setValue(latestModification.getTime());
                }
                sleep(SECONDS_OF_CHECK_PERIODICITY * 1000);
                //} catch (RepositoryException ignore1) {
                //} catch (InterruptedException ignore2) {
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }
        logger.info("RepositoryWatcher finished.");
    }

    public void finish() {
        continueRunning = false;
        super.interrupt();
    }
}
