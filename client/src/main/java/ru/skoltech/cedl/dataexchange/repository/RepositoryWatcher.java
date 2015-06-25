package ru.skoltech.cedl.dataexchange.repository;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private BooleanProperty repositoryNewer = new SimpleBooleanProperty();

    private BooleanProperty workingCopyModified = new SimpleBooleanProperty();

    public RepositoryWatcher(Project project) {
        this.project = project;
        this.repository = RepositoryFactory.getDatabaseRepository();
    }

    @Override
    public void run() {
        while (continueRunning) {
            long timeOfModificationLoaded = project.getLatestModification();
            long systemModelId = project.getStudy().getSystemModel().getId();
            try {
                SystemModel systemModel = repository.loadSystemModel(systemModelId);
                Timestamp latestModification = systemModel.findLatestModification();
                boolean repoNewer = latestModification.getTime() > timeOfModificationLoaded;
                logger.info(latestModification + " > " + timeOfModificationLoaded + " = " + repoNewer);
                repositoryNewer.setValue(repoNewer);
            } catch (RepositoryException ignore) {
            }
            try {
                sleep(SECONDS_OF_CHECK_PERIODICITY * 1000);
            } catch (InterruptedException ignore) {
            }
        }
        logger.info("RepositoryWatcher finished.");
    }

    public void finish() {
        continueRunning = false;
        super.interrupt();
    }

    public boolean getRepositoryNewer() {
        return repositoryNewer.get();
    }

    public BooleanProperty repositoryNewerProperty() {
        return repositoryNewer;
    }

    public boolean getWorkingCopyModified() {
        return workingCopyModified.get();
    }

    public BooleanProperty workingCopyModifiedProperty() {
        return workingCopyModified;
    }
}
