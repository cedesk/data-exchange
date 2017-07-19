package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.ApplicationPackage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service which monitor application's updates in the repository.
 *
 * Created by n.groshkov on 18-Jul-17.
 */
public interface UpdateService {

    Optional<ApplicationPackage> getLatestVersionAvailable();

    ApplicationPackage getLatest(List<String> fileNames);

    List<String> extractFileNames(File file) throws IOException;
}
