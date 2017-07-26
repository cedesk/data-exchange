/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.file;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A simple class which can monitor files and notify interested parties
 * (i.e. listeners) of file changes.
 * <p>
 * This class is kept lean by only keeping methods that are actually being
 * called.
 */
public class SimpleDirectoryWatchService implements DirectoryWatchService, Runnable {

    private static final Logger logger = Logger.getLogger(SimpleDirectoryWatchService.class);
    private final WatchService watchService;
    private final AtomicBoolean isRunning;
    private final ConcurrentMap<WatchKey, Path> watchKeyToDirPathMap;
    private final ConcurrentMap<Path, Set<OnFileChangeListener>> dirPathToListenersMap;
    private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> listenerToFilePatternsMap;
    private Thread runnerThread;

    /**
     * A simple no argument constructor for creating a <code>SimpleDirectoryWatchService</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    private SimpleDirectoryWatchService() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        isRunning = new AtomicBoolean(false);
        watchKeyToDirPathMap = newConcurrentMap();
        dirPathToListenersMap = newConcurrentMap();
        listenerToFilePatternsMap = newConcurrentMap();
    }

    public static SimpleDirectoryWatchService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    private static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(newConcurrentMap());
    }

    public static PathMatcher matcherForGlobExpression(String globPattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
    }

    public static boolean matchesAny(Path input, Set<PathMatcher> patterns) {
        if (patterns != null) {
            for (PathMatcher pattern : patterns) {
                if (pattern.matches(input)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Path getDirPath(WatchKey key) {
        return watchKeyToDirPathMap.get(key);
    }

    private Set<OnFileChangeListener> getListeners(Path dir) {
        return dirPathToListenersMap.get(dir);
    }

    private Set<OnFileChangeListener> matchedListeners(Path dir, Path file) {
        return getListeners(dir)
                .stream()
                .filter(listener -> matchesAny(file, listenerToFilePatternsMap.get(listener)))
                .collect(Collectors.toSet());
    }

    private void notifyListeners(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind eventKind = event.kind();

            // Overflow occurs when the watch event queue is overflown
            // with events.
            if (eventKind.equals(OVERFLOW)) {
                // TODO: Notify all listeners.
                return;
            }

            WatchEvent<Path> pathEvent = cast(event);
            Path filePath = pathEvent.context();
            Path dirPath = getDirPath(key);

            File file = new File(dirPath.toFile(), filePath.toString());

            if (eventKind.equals(ENTRY_CREATE)) {
                matchedListeners(dirPath, filePath)
                        .forEach(listener -> listener.onFileCreate(file));
            } else if (eventKind.equals(ENTRY_MODIFY)) {
                matchedListeners(dirPath, filePath)
                        .forEach(listener -> listener.onFileModify(file));
            } else if (eventKind.equals(ENTRY_DELETE)) {
                matchedListeners(dirPath, filePath)
                        .forEach(listener -> listener.onFileDelete(file));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(OnFileChangeListener listener, String dirPath, String... globPatterns)
            throws IOException {
        Path dir = Paths.get(dirPath);

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(dirPath + " is not a directory.");
        }

        if (!dirPathToListenersMap.containsKey(dir)) {
            // May throw
            WatchKey key = dir.register(
                    watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
            );

            watchKeyToDirPathMap.put(key, dir);
            dirPathToListenersMap.put(dir, newConcurrentSet());
        }

        getListeners(dir).add(listener);

        Set<PathMatcher> patterns = newConcurrentSet();

        for (String globPattern : globPatterns) {
            patterns.add(matcherForGlobExpression(globPattern));
        }

        if (patterns.isEmpty()) {
            patterns.add(matcherForGlobExpression("*")); // Match everything if no filter is found
        }

        listenerToFilePatternsMap.put(listener, patterns);

        logger.info("Watching files matching " + Arrays.toString(globPatterns)
                + " under " + dirPath + " for changes.");
    }

    /**
     * Empty the list of registered directory watchers.
     */
    public void clear() {
        dirPathToListenersMap.clear();
        watchKeyToDirPathMap.clear();
        listenerToFilePatternsMap.clear();
    }

    /**
     * Start this <code>SimpleDirectoryWatchService</code> instance by spawning a new thread.
     *
     * @see #stop()
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            runnerThread = new Thread(this, DirectoryWatchService.class.getSimpleName());
            runnerThread.start();
        }
    }

    /**
     * Stop this <code>SimpleDirectoryWatchService</code> thread.
     * The killing happens lazily, giving the running thread an opportunity
     * to finish the work at hand.
     *
     * @see #start()
     */
    public void stop() {
        logger.info("Stopping file watcher service...");
        isRunning.set(false);
        try {
            runnerThread.join();
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.info("Starting file watcher service.");

        while (isRunning.get()) {
            WatchKey key;
            try {
                key = watchService.poll(10, TimeUnit.MILLISECONDS);
                if (key == null && isRunning.get()) {
                    continue;
                } else if (!isRunning.get()) {
                    break;
                }
            } catch (InterruptedException | ClosedWatchServiceException e) {
                logger.info("DirectoryWatchService service interrupted.");
                break;
            }

            if (null == getDirPath(key)) {
                logger.error("Watch key not recognized.");
                continue;
            }

            notifyListeners(key);

            // Reset key to allow further events for this key to be processed.
            boolean valid = key.reset();
            if (!valid) {
                watchKeyToDirPathMap.remove(key);
            }
        }
        try {
            watchService.close();
        } catch (IOException ignore) {
        }

        isRunning.set(false);
        logger.info("Stopped file watcher service.");
    }

    private static class SingletonHolder {
        /**
         * The singleton instance.
         */
        private static final SimpleDirectoryWatchService INSTANCE;

        static {
            try {
                INSTANCE = new SimpleDirectoryWatchService();
            } catch (IOException | UnsupportedOperationException e) {
                throw new ExceptionInInitializerError("Unable to start "
                        + DirectoryWatchService.class.getSimpleName() + " instance.");
            }
        }
    }
}