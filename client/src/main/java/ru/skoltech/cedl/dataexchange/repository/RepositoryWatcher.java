package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
                    loadRepositoryStudy();
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

    public void loadRepositoryStudy() {
        LocalTime startTime = LocalTime.now();
        project.loadRepositoryStudy();
        long loadDuration = startTime.until(LocalTime.now(), ChronoUnit.MILLIS);
        logger.info("loaded repository study (" + loadDuration + "ms)");

        Study localStudy = project.getStudy();
        Study repositoryStudy = project.getRepositoryStudy();
        User user = project.getUser();
        if (repositoryStudy != null) {
            StudySettings localSettings = localStudy.getStudySettings();
            StudySettings remoteSettings = repositoryStudy.getStudySettings();
            if (!localSettings.equals(remoteSettings)) {
                logger.debug("updating studySettings");
                project.setStudySettings(remoteSettings);
            }
            UserRoleManagement localURM = localStudy.getUserRoleManagement();
            UserRoleManagement remoteURM = repositoryStudy.getUserRoleManagement();
            if (!localURM.isAdmin(user) && !localURM.equals(remoteURM)) {
                logger.debug("updating userRoleManagement");
                project.setUserRoleManagement(remoteURM);
            }
        }
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
