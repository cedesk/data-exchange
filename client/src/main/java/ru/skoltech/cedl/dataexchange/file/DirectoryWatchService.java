/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.file;

import java.io.File;
import java.io.IOException;

/**
 * Interface definition of a simple directory watch service.
 * <p>
 * Implementations of this interface allow interested parties to <em>listen</em>
 * to file system events coming from a specific directory.
 */
public interface DirectoryWatchService {

    /**
     * Notifies the implementation of <em>this</em> interface that <code>dirPath</code>
     * should be monitored for file system events. If the changed file matches any
     * of the <code>globPatterns</code>, <code>listener</code> should be notified.
     *
     * @param listener     The listener.
     * @param dirPath      The directory path.
     * @param globPatterns Zero or more file patterns to be matched against file names.
     *                     If none provided, matches <em>any</em> file.
     * @throws IOException If <code>dirPath</code> is not a directory.
     */
    void register(OnFileChangeListener listener, String dirPath, String... globPatterns)
            throws IOException;

    /**
     * Interface definition for a callback to be invoked when a file under
     * watch is changed.
     */
    interface OnFileChangeListener {

        /**
         * Called when the file is created.
         *
         * @param createdFile The file path.
         */
        default void onFileCreate(File createdFile) {
        }

        /**
         * Called when the file is modified.
         *
         * @param modifiedFile The file path.
         */
        default void onFileModify(File modifiedFile) {
        }

        /**
         * Called when the file is deleted.
         *
         * @param deletedFile The file path.
         */
        default void onFileDelete(File deletedFile) {
        }
    }
}