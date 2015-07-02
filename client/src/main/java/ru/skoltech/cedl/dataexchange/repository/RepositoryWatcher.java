package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class RepositoryWatcher extends Thread {

    public static final long SECONDS_OF_CHECK_PERIODICITY = 10;

    private static final Logger logger = Logger.getLogger(RepositoryWatcher.class);

    private final Project project;

    private boolean continueRunning = true;

    public RepositoryWatcher(Project project) {
        this.project = project;
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

    public void finish() {
        continueRunning = false;
        super.interrupt();
    }
}
