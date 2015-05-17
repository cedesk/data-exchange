package ru.skoltech.cedl.dataexchange.repository.svn;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;

/**
 * Created by dknoll on 24/03/15.
 */
public class RepositoryUtils {

    static final String DEFAULT_USER_NAME = "anonymous";
    static final String DEFAULT_PASSWORD = "anonymous";

    public static String makeUrlFromPath(File path) {
        return "file:///" + path.toString();
    }

    public static boolean checkRepository(String url, String dataFileName) {
        return checkRepository(url, dataFileName, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
    }

    public static boolean checkRepository(String url, String dataFileName, String userName, String password) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            File confDir = SVNWCUtil.getDefaultConfigurationDirectory();
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(confDir, userName, password.toCharArray(), false);
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
}
