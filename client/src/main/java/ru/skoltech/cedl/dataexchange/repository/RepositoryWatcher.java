package ru.skoltech.cedl.dataexchange.repository;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ru.skoltech.cedl.dataexchange.structure.Project;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class RepositoryWatcher extends Thread {

    public static final long DEFAULT_TIMING = 10;

    private long timing = DEFAULT_TIMING;

    private boolean continueRunning = true;

    private Repository repository;

    private BooleanProperty repositoryNewer = new SimpleBooleanProperty();

    private BooleanProperty workingCopyModified = new SimpleBooleanProperty();

    public RepositoryWatcher(Project project) {
        this.repository = RepositoryFactory.getDefaultRepository();
    }

    @Override
    public void run() {
        while (continueRunning) {
            /*
            boolean remoteRepositoryNewer = repositoryStorage.isRemoteRepositoryNewer();
            repositoryNewer.setValue(remoteRepositoryNewer);
            boolean wcCopyModified = repositoryStorage.isWorkingCopyModified(dataFile);
            workingCopyModified.setValue(wcCopyModified);
            */
            try {
                sleep(timing * 1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        System.out.println("RepositoryWatcher finished.");
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
