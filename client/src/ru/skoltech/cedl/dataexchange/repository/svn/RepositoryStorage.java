package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.*;
import java.util.Scanner;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class RepositoryStorage {

    private static final String DEFAULT_NAME = "anonymous";
    private static final String DEFAULT_PASSWORD = "anonymous";
    private static final long INVALID_REVISION = -1L;

    private String url;
    private String filePath;
    private long checkedoutRevision = -1L;
    private SVNRepository repository;

    static {
        setupLibrary();
    }

    public static String makeUrlFromPath(File path) {
        return "file:///" + path.toString();
    }

    public static boolean checkRepository(String url) {
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(DEFAULT_NAME, DEFAULT_PASSWORD);
            repository.setAuthenticationManager(authManager);

            SVNNodeKind nodeKind = repository.checkPath(StorageUtils.getDataFileName(), -1);
            if (nodeKind == SVNNodeKind.FILE) {
                return true;
            }
        } catch (SVNException e) {
            //ignore
        }
        return false;
    }

    /*
     * Initializes the library to work with a repository via different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    public RepositoryStorage(String url, String filePath) throws SVNException {
        this.url = url;
        this.filePath = filePath;

        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(DEFAULT_NAME, DEFAULT_PASSWORD);
        repository.setAuthenticationManager(authManager);
    }

    public String getUrl() {
        return url;
    }

    public long getCheckedoutRevision() {
        if(checkedoutRevision == INVALID_REVISION) {
            long revisionfromFile = readCheckedoutRevision();
            if(revisionfromFile != INVALID_REVISION) {
                checkedoutRevision = revisionfromFile;
            }
        }
        return checkedoutRevision;
    }

    private void setCheckedoutRevision(long checkedoutRevision) {
        this.checkedoutRevision = checkedoutRevision;
        writeCheckedoutRevision(checkedoutRevision);
    }

    private long readCheckedoutRevision() {
        long revision = INVALID_REVISION;
        try(FileReader fr = new FileReader(StorageUtils.getCheckedoutRivisionFile())) {
            Scanner scanner = new Scanner(fr);
            scanner.useDelimiter(":");
            revision = scanner.nextLong();
            String md5 = scanner.next();
        } catch (IOException e) {
            System.err.println("Error reading the revision file.");
        }
        return revision;
    }

    private void writeCheckedoutRevision(long revision) {
        try(FileWriter fw = new FileWriter(StorageUtils.getCheckedoutRivisionFile())) {
            PrintWriter pw = new PrintWriter(fw);
            String md5 = "md5";
            pw.printf("%d:%s", revision, md5);
        } catch (IOException e) {
            System.err.println("Error writing the revision file.");
        }
    }

    public boolean isRemoteRepositoryNewer() {
        try {
            return getCheckedoutRevision() < repository.getLatestRevision();
        } catch (SVNException e) {
            System.err.println("Error checking repository revision.");
        }
        return false;
    }

    public boolean checkoutFile() {
        if(!isRemoteRepositoryNewer()) {
            return false;
        }

        File checkoutFile = StorageUtils.getCheckedoutDataFile();
        OutputStream baos = null;
        try {
            baos = new FileOutputStream(checkoutFile);
        } catch (FileNotFoundException ioe) {
            System.err
                    .println("error writing working copy: '"
                            + checkoutFile.toString() + "'\n\t" + ioe.getMessage());
            return false;
        }

        try {
            SVNNodeKind nodeKind = repository.checkPath(filePath, -1);
            if (nodeKind == SVNNodeKind.NONE) {
                System.err.println("Error finding file in repository. '" + repository.getFullPath("") + "', '" + filePath + "'");
                return false;
            }

            SVNProperties fileProperties = new SVNProperties();
            repository.getFile(filePath, -1, fileProperties, baos);

            setCheckedoutRevision(repository.getLatestRevision());

        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + url + "', '" + filePath + "': " + svne.getMessage());
            return false;
        }
        return true;
    }


}
