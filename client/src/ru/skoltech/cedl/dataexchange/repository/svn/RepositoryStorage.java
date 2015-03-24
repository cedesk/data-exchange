package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class RepositoryStorage {

    private SVNURL svnUrl;
    private File filePath;

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

    public RepositoryStorage(String url, File filePath) throws SVNException {
        this.svnUrl = SVNURL.parseURIEncoded(url);
        this.filePath = filePath;

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

        try {
            SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            SVNRevision revision = SVNRevision.HEAD;
            SVNDepth depth = SVNDepth.INFINITY;
            boolean allowUnversionedObstructions = false;
            long rev = updateClient.doCheckout(svnUrl, filePath, revision, revision, depth, allowUnversionedObstructions);
            return true;
        } catch (SVNException svne) {
            System.err
                    .println("error accessing SVN repository '"
                            + svnUrl.toString() + "', '" + filePath.toString() + "': " + svne.getMessage());
            return false;
        }
    }

}
