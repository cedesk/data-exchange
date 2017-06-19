package ru.skoltech.cedl.dataexchange.logging;

import org.apache.log4j.FileAppender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by n.groshkov on 19-Jun-17.
 */
public class StartRollingFileAppender extends FileAppender {

    /**
     The path to the log file.
     */
    protected String directory = null;

    /**
     The file name pattern of the log file.
     */
    protected String filePattern = null;

    /**
     The suffix of the log file name.
     */
    protected String fileSuffix = null;

    /**
     There is one backup file by default.
     */
    protected int  maxBackupIndex  = 1;

    @Override
    public void activateOptions() {
        List<String> parts = new ArrayList(Arrays.asList(filePattern.split("\\.")));
        String filePrefix = String.join(".", parts.subList(0, parts.size() - 1));
        String extension = parts.get(parts.size() - 1);
        this.fileName = directory + "/" + filePrefix + "." + fileSuffix + "." + extension;
        removeOldFiles(filePrefix, extension);
        super.activateOptions();
    }

    private void removeOldFiles(String filePrefix, String extension) {
        File dirFile = new File(directory);
        List<String> oldLogFileNames = new ArrayList(Arrays.asList(dirFile.list((dir, file) ->  {
            return file.startsWith(filePrefix) && file.endsWith(extension);
        })));

        oldLogFileNames.sort(Comparator.naturalOrder());
        if (maxBackupIndex > oldLogFileNames.size()) {
            return;
        }
        List<String> toDeleteFileNames = oldLogFileNames.subList(maxBackupIndex - 1, oldLogFileNames.size());

        toDeleteFileNames.stream().map(s -> new File(dirFile, s)).forEach(File::delete);
    }

    /**
     Returns the value of the <b>Directory</b> option.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     Set the directory of file name.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     Returns the value of the <b>FilePattern</b> option.
     */
    public String getFilePattern() {
        return filePattern;
    }

    /**
     Set the pattern of file name.
     */
    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    /**
     Returns the value of the <b>FileSuffix</b> option.
     */
    public String getFileSuffix() {
        return fileSuffix;
    }

    /**
     Set the suffix of backup file name, the part which adds before extension.
     */
    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    /**
     Returns the value of the <b>MaxBackupIndex</b> option.
     */
    public int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    /**
     Set the maximum number of backup files to keep around.

     <p>The <b>MaxBackupIndex</b> option determines how many backup
     files are kept before the oldest is erased. This option takes
     a positive integer value. If set to zero, then there will be no
     backup files and the log file will be truncated when it reaches
     <code>MaxFileSize</code>.
     */
    public void setMaxBackupIndex(int maxBackups) {
        this.maxBackupIndex = maxBackups;
    }

}
