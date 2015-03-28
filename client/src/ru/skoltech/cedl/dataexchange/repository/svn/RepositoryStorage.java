package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import ru.skoltech.cedl.dataexchange.StatusLogger;

import java.io.File;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class RepositoryStorage {

    private SVNURL svnUrl;
    private File wcPath;

    private SVNClientManager svnClientManager;

    static {
        setupLibrary();
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

    public RepositoryStorage(String url, File wcPath, String userName, String password) throws SVNException {
        this.svnUrl = SVNURL.parseURIEncoded(url);
        this.wcPath = wcPath;

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(
                userName, password);
        svnClientManager = SVNClientManager.newInstance();
        svnClientManager.setAuthenticationManager(authManager);
    }

    public String getUrl() {
        return svnUrl.toString();
    }


    public long getRepositoryRevisionNumber() {
        SVNRepository svnRepository = null;
        try {
            svnRepository = SVNRepositoryFactory.create(svnUrl);
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
            SVNStatusType statusType = svnStatus.getNodeStatus();
            return statusType == SVNStatusType.STATUS_MODIFIED;
        } catch (SVNException e) {
            System.err.println("Error checking working copy revision.");
        }
        return false;
    }

    public boolean checkoutFile() {

        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        try {
            long rev = updateClient.doCheckout(svnUrl, wcPath, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.IMMEDIATES, false);
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
            long rev = updateClient.doUpdate(wcPath, SVNRevision.HEAD, SVNDepth.IMMEDIATES, false, false);
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
        SVNDepth depth = SVNDepth.IMMEDIATES;

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

}
