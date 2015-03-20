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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class RepositoryStorage {

    private static final String DEFAULT_NAME = "anonymous";
    private static final String DEFAULT_PASSWORD = "anonymous";

    private String url;
    private String filePath;
    private long checkedoutRevision;
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

    public boolean isRemoteRepositoryNewer() {
        try {
            return checkedoutRevision < repository.getLatestRevision();
        } catch (SVNException e) {
            System.err.println("Error checking repository revision.");
        }
        return false;
    }

    public void checkoutFile() {
        File checkoutFile = StorageUtils.getCheckedoutDataFile();
        OutputStream baos = null;
        try {
            baos = new FileOutputStream(checkoutFile);
        } catch (FileNotFoundException ioe) {
            System.err
                    .println("error writing working copy: '"
                            + checkoutFile.toString() + "'\n\t" + ioe.getMessage());
            return;
        }

        try {
            SVNNodeKind nodeKind = repository.checkPath(filePath, -1);
            if (nodeKind == SVNNodeKind.NONE) {
                System.err.println("Error finding file in repository. '" + repository.getFullPath("") + "', '" + filePath + "'");
                return;
            }

            SVNProperties fileProperties = new SVNProperties();
            repository.getFile(filePath, -1, fileProperties, baos);

            checkedoutRevision = repository.getLatestRevision();

        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + url + "', '" + filePath + "': " + svne.getMessage());
        }
    }


}
