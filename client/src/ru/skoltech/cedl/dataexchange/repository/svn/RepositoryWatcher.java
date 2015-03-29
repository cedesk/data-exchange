package ru.skoltech.cedl.dataexchange.repository.svn;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class RepositoryWatcher extends Thread {

    public final long DEFAULT_TIMING = 10;

    private long timing = DEFAULT_TIMING;

    private boolean continueRunning = true;

    private RepositoryStorage repositoryStorage;

    private BooleanProperty repositoryNewer = new SimpleBooleanProperty();

    public RepositoryWatcher(RepositoryStorage repositoryStorage) {
        this.repositoryStorage = repositoryStorage;
    }

    public RepositoryWatcher(RepositoryStorage repositoryStorage, long timing) {
        this.repositoryStorage = repositoryStorage;
        this.timing = timing;
    }

    @Override
    public void run() {
        while (continueRunning) {
            boolean remoteRepositoryNewer = repositoryStorage.isRemoteRepositoryNewer();
            repositoryNewer.setValue(remoteRepositoryNewer);
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
}
