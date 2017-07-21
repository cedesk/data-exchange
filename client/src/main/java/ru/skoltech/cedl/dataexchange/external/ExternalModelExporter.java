/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.external;

import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.Closeable;

/**
 * Created by D.Knoll on 23.07.2015.
 */
public interface ExternalModelExporter extends Closeable {

    void setValue(Project project, String target, Double value) throws ExternalModelException;

    void flushModifications(Project project, ExternalModelFileWatcher externalModelFileWatcher) throws ExternalModelException;
}
