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

package ru.skoltech.cedl.dataexchange.logging;

import org.apache.log4j.FileAppender;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nikolay Groshkov on 19-Jun-17.
 */
public class StartRollingFileAppender extends FileAppender {

    /**
     * The path to the log file.
     */
    protected String directory = null;

    /**
     * The file name pattern of the log file.
     */
    protected String filePattern = null;

    /**
     * The suffix pattern of the log file name.
     */
    protected String fileSuffixPattern = null;

    /**
     * There is one backup file by default.
     */
    protected int maxBackupIndex = 1;

    /**
     * Current time for new log file.
     */
    private Date logStartDate = new Date();

    /**
     * Returns the value of the <b>Directory</b> option.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Set the directory of file name.
     */
    public void setDirectory(String directory) {
        File dirFile = new File(directory);
        if (!dirFile.isAbsolute()) {
            String userHome = System.getProperty("user.home");
            dirFile = new File(userHome, directory);
        }

        System.out.println("logging directory = " + dirFile.getAbsolutePath());
        this.directory = dirFile.getAbsolutePath();
    }

    /**
     * Returns the value of the <b>FilePattern</b> option.
     */
    public String getFilePattern() {
        return filePattern;
    }

    /**
     * Set the pattern of file name.
     */
    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    /**
     * Returns the value of the <b>FileSuffixPattern</b> option.
     */
    public String getFileSuffixPattern() {
        return fileSuffixPattern;
    }

    /**
     * Set the suffix pattern of backup file name, the part which adds before extension.
     */
    public void setFileSuffixPattern(String fileSuffixPattern) {
        this.fileSuffixPattern = fileSuffixPattern;
    }

    /**
     * Returns the value of the <b>MaxBackupIndex</b> option.
     */
    public int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    /**
     * Set the maximum number of backup files to keep around.
     * <p>
     * <p>The <b>MaxBackupIndex</b> option determines how many backup
     * files are kept before the oldest is erased. This option takes
     * a positive integer value. If set to zero, then there will be no
     * backup files and the log file will be truncated when it reaches
     * <code>MaxFileSize</code>.
     */
    public void setMaxBackupIndex(int maxBackups) {
        this.maxBackupIndex = maxBackups;
    }

    @Override
    public void activateOptions() {
        List<String> parts = new ArrayList<String>(Arrays.asList(filePattern.split("\\.")));
        String filePrefix = String.join(".", parts.subList(0, parts.size() - 1));
        String extension = parts.get(parts.size() - 1);
        String fileSuffix = new SimpleDateFormat(fileSuffixPattern).format(logStartDate);
        this.fileName = directory + "/" + filePrefix + "." + fileSuffix + "." + extension;
        this.removeOldFiles(filePrefix, extension);
        super.activateOptions();
    }

    private void removeOldFiles(String filePrefix, String extension) {
        File directoryFile = new File(directory);

        List<String> oldLogFileNames = new ArrayList(Arrays.asList(directoryFile.list((dir, file) ->
                file.startsWith(filePrefix) && file.endsWith(extension)
        )));

        oldLogFileNames.sort(Comparator.reverseOrder());
        if (maxBackupIndex > oldLogFileNames.size()) {
            return;
        }
        List<String> toDeleteFileNames = oldLogFileNames.subList(maxBackupIndex - 1, oldLogFileNames.size());

        toDeleteFileNames.stream().map(s -> new File(directoryFile, s)).forEach(File::delete);
    }

}
