package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import ru.skoltech.cedl.dataexchange.StatusLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class RepositoryStorage {

    static final String DEFAULT_USER_NAME = "anonymous";
    static final String DEFAULT_PASSWORD = "anonymous";

    static {
        setupLibrary();
    }

    private final SVNURL svnUrl;
    private final File wcPath;
    private final SVNClientManager svnClientManager;
    private final ISVNAuthenticationManager authManager;

    public RepositoryStorage(String url, File wcPath, String userName, String password) throws SVNException {
        this.svnUrl = SVNURL.parseURIEncoded(url);
        this.wcPath = wcPath;

        File confDir = SVNWCUtil.getDefaultConfigurationDirectory();
        char[] pwd = password != null ? password.toCharArray() : new char[]{};
        authManager = SVNWCUtil.createDefaultAuthenticationManager(confDir, userName, pwd, false);
        svnClientManager = SVNClientManager.newInstance();
        svnClientManager.setAuthenticationManager(authManager);
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

    public static String makeUrlFromPath(File path) {
        return "file:///" + path.toString();
    }

    public static boolean checkRepository(String url, String dataFileName) {
        return checkRepository(url, DEFAULT_USER_NAME, DEFAULT_PASSWORD, dataFileName);
    }

    /**
     * This method tries to connect to the repository with the specified credentials and checks whether it contains the given file.
     *
     * @param url
     * @param userName
     * @param password
     * @param dataFileName
     * @return true when the repository exists and contains the given file
     */
    public static boolean checkRepository(String url, String userName, String password, String dataFileName) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            File confDir = SVNWCUtil.getDefaultConfigurationDirectory();
            char[] pwd = password != null ? password.toCharArray() : new char[]{};
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(confDir, userName, pwd, false);
            repository.setAuthenticationManager(authManager);

            SVNNodeKind nodeKind = repository.checkPath(dataFileName, -1);
            if (nodeKind == SVNNodeKind.FILE) {
                return true;
            }
        } catch (SVNAuthenticationException ae) {
            System.err.println("SVN Authentication Error.");
        } catch (SVNException e) {
            System.err.println("SVNException: " + e.getMessage());
        }
        return false;
    }

    public String getUrl() {
        return svnUrl.toString();
    }

    public long getRepositoryRevisionNumber() {
        SVNRepository svnRepository = null;
        try {
            svnRepository = SVNRepositoryFactory.create(svnUrl);
            svnRepository.setAuthenticationManager(authManager);
            return svnRepository.getLatestRevision();
        } catch (SVNException e) {
            System.err.println("Error checking repository.");
        } finally {
            if (svnRepository != null) {
                svnRepository.closeSession();
            }
        }
        return -1;
    }

    public long getWorkingCopyRevisionNumber() {
        try {
            SVNStatusClient statusClient = svnClientManager.getStatusClient();
            SVNStatus svnStatus = statusClient.doStatus(wcPath, true, false);
            return svnStatus.getRevision().getNumber();
        } catch (SVNException e) {
            System.err.println("Error checking working copy revision.");
        }
        return -1;
    }

    public boolean isRemoteRepositoryNewer() {
        long repositoryRevision = getRepositoryRevisionNumber();
        long workingCopyRevision = getWorkingCopyRevisionNumber();
        System.out.println("repositoryRevision: " + repositoryRevision + ", workingCopyRevision: " + workingCopyRevision);
        return repositoryRevision > workingCopyRevision;
    }

    public boolean isWorkingCopyModified(File dataFile) {
        try {
            SVNStatusClient statusClient = svnClientManager.getStatusClient();
            SVNStatus svnStatus = statusClient.doStatus(dataFile, true, false);
            boolean wcModified = svnStatus.getContentsStatus() == SVNStatusType.STATUS_MODIFIED;
            System.out.println("workingCopyModified: " + wcModified);
            return wcModified;
        } catch (SVNException e) {
            System.err.println("Error checking working copy revision.");
        }
        return false;
    }

    public boolean checkoutFile() {

        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        try {
            long rev = updateClient.doCheckout(svnUrl, wcPath, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "'\n" + svne.getMessage());
            return false;
        }
    }

    public boolean updateFile() {
        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        try {
            long rev = updateClient.doUpdate(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error updating working copy from SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "'\n" + svne.getMessage());
        }
        return false;
    }

    public boolean commitFile(String commitMessage) {
        SVNProperties revisionProperties = new SVNProperties();
        SVNDepth depth = SVNDepth.INFINITY;

        try {
            SVNCommitClient commitClient = svnClientManager.getCommitClient();
            SVNCommitInfo svnCommitInfo = commitClient.doCommit(new File[]{wcPath}, false,
                    commitMessage, revisionProperties, null, false, true, depth);
            long newRevision = svnCommitInfo.getNewRevision();
            if (newRevision < 0) {
                StatusLogger.getInstance().log("Nothing to be committed.");
            }
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error committing to SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "'\n" + svne.getMessage());
            return false;
        }
    }

    public boolean cleanup() {
        SVNWCClient wcClient = svnClientManager.getWCClient();
        try {
            wcClient.doCleanup(wcPath);
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error committing to SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "'\n" + svne.getMessage());
            return false;
        }
    }

    public InputStream getFileContentFromRepository(String fileName) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            SVNRepository repository = SVNRepositoryFactory.create(svnUrl);
            repository.setAuthenticationManager(authManager);
            SVNProperties fileProperties = new SVNProperties();
            repository.getFile(fileName, -1, fileProperties, baos);

        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + svnUrl + "', '" + fileName + "': " + svne.getMessage());
            return null;
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RepositoryStorage{");
        sb.append("svnUrl=").append(svnUrl);
        sb.append(", wcPath=").append(wcPath);
        sb.append('}');
        return sb.toString();
    }

}
