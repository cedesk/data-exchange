/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.ApplicationPackage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service which monitor application's updates in the repository.
 *
 * Created by Nikolay Groshkov on 18-Jul-17.
 */
public interface UpdateService {

    Optional<ApplicationPackage> getLatestVersionAvailable();

    ApplicationPackage getLatest(List<String> fileNames);

    List<String> extractFileNames(File file) throws IOException;
}
