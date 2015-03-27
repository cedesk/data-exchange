package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

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

    public RepositoryStorage(String url, File wcPath) throws SVNException {
        this.svnUrl = SVNURL.parseURIEncoded(url);
        this.wcPath = wcPath;

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(RepositoryUtils.DEFAULT_NAME, RepositoryUtils.DEFAULT_PASSWORD);
        svnClientManager = SVNClientManager.newInstance();
        svnClientManager.setAuthenticationManager(authManager);
    }

    public String getUrl() {
        return svnUrl.toString();
    }

    /*
        public boolean isRemoteRepositoryNewer() {

            File file = StorageUtils.getCheckedoutDataFile();
            boolean remote = true;
            boolean collectParentExternals = false;
            try {
                svnClientManager.getLookClient();
                SVNStatus svnStatus = svnClientManager.getStatusClient().doStatus(file, remote, collectParentExternals);
                SVNRevision committedRevision = svnStatus.getCommittedRevision();
                SVNRevision revision = svnStatus.getRevision();
                return committedRevision.getNumber() > revision.getNumber();
            } catch (SVNException e) {
                System.err.println("Error checking repository revision.");
            }
            return false;
        }
    */
    public boolean checkoutFile() {

        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        try {
            long rev = updateClient.doCheckout(svnUrl, wcPath, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "': " + svne.getMessage());
            return false;
        }
    }

    public void updateFile() {

        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        try {
            long rev = updateClient.doUpdate(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
        } catch (SVNException svne) {
            System.err
                    .println("error updating working copy from SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "': " + svne.getMessage());
        }
    }

    public void commitFile(String commitMessage, String author) {
        SVNProperties revisionProperties = new SVNProperties();
        revisionProperties.put(SVNProperty.LAST_AUTHOR, author);
        SVNDepth depth = SVNDepth.INFINITY;

        try {
            SVNCommitInfo svnCommitInfo = svnClientManager.getCommitClient().doCommit(new File[]{wcPath}, false,
                    commitMessage, revisionProperties, null, false, true, depth);
            System.out.println(svnCommitInfo.getNewRevision());
        } catch (SVNException svne) {
            System.err
                    .println("error committing to SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "': " + svne.getMessage());
        }
    }

    public void showStatus() {
        try {
            SVNStatus dataFileStatus = svnClientManager.getStatusClient().doStatus(wcPath, false, false);
            // TODO: implement handling of status
            // (new StatusHandler(false)).handleStatus(dataFileStatus);
        } catch (SVNException svne) {
            System.err
                    .println("error checking status with SVN repository '"
                            + svnUrl.toString() + "', '" + wcPath.toString() + "': " + svne.getMessage());
        }
    }

}
