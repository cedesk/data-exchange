package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class RepositoryWatcher extends Thread {

    public static final long SECONDS_OF_CHECK_PERIODICITY = 10;

    private static final Logger logger = Logger.getLogger(RepositoryWatcher.class);

    private final Project project;

    private AtomicBoolean quitRunning = new AtomicBoolean(false);

    private AtomicBoolean pausedRunning = new AtomicBoolean(false);

    public RepositoryWatcher(Project project) {
        this.project = project;
    }

    @Override
    public void run() {
        try {
            sleep(SECONDS_OF_CHECK_PERIODICITY * 100); // willingly shorter
        } catch (InterruptedException ignore) {
        }
        while (!quitRunning.get()) {
            try {
                if (!pausedRunning.get() &&
                        project.getStudy() != null &&
                        project.isStudyInRepository()) {
                    logger.info("load repository study");
                    project.loadRepositoryStudy();
                }
                sleep(SECONDS_OF_CHECK_PERIODICITY * 1000);
                //} catch (RepositoryException ignore1) {
            } catch (InterruptedException ignore) {
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }
        logger.info("RepositoryWatcher finished.");
    }

    public void pause() {
        pausedRunning.set(true);
    }

    public void unpause() {
        pausedRunning.set(false);
    }

    public void finish() {
        quitRunning.set(true);
        super.interrupt();
        try {
            super.join();
        } catch (InterruptedException ignore) {
        }
    }
}
