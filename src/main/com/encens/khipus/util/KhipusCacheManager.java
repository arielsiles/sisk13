package com.encens.khipus.util;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.io.File;
import java.io.IOException;

/**
 * Encens S.R.L.
 * Util to manage KHIPUS temp cache files
 *
 * @author
 * @version $Id: KhipusCacheManager.java  18-may-2010 19:14:27$
 */
public final class KhipusCacheManager {

    private KhipusCacheManager() {
    }

    private static Log log = Logging.getLog(KhipusCacheManager.class);

    public static String PATH_KHIPUS_DIR = KhipusCacheManager.init();

    private static final String KHIPUS_DIR = "khipus";
    private static final String SESSIONS_DIR = "sessions";
    private static final String CONTRACT_DOCUMENT_DIR = "contractDocument";
    private static final String TEMPCONTRACT_DIR = "temContract";

    private static String init() {
        try {
            String path = new File(System.getProperty("java.io.tmpdir") + Constants.FILE_SEPARATOR + KHIPUS_DIR).getCanonicalPath() + Constants.FILE_SEPARATOR;
            log.debug("PATH:" + path);
            return path;
        } catch (IOException e) {
            log.error("Error in company path..", e);
        }
        return "/";
    }

    public static String pathSessionFolder(boolean slash) {
        return PATH_KHIPUS_DIR + SESSIONS_DIR + (slash ? Constants.FILE_SEPARATOR : "");
    }

    public static String pathSessionFolder(Object sessionId, boolean slash) {
        return pathSessionFolder(true) + sessionId + (slash ? Constants.FILE_SEPARATOR : "");
    }

    public static String pathContractDocumentFolder(Object sessionId, boolean slash) {
        return pathSessionFolder(sessionId, true) + CONTRACT_DOCUMENT_DIR + (slash ? Constants.FILE_SEPARATOR : "");
    }

    public static String pathContractDocumentFolderCreateIfNotExist(Object sessionId, boolean slash) {
        String path = pathContractDocumentFolder(sessionId, slash);
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        return path;
    }

    public static String pathTempContractFolder(Object sessionId, boolean slash) {
        return pathContractDocumentFolder(sessionId, true) + TEMPCONTRACT_DIR + (slash ? Constants.FILE_SEPARATOR : "");
    }

    public static String pathTempContractFolderCreateIfNotExist(Object sessionId, boolean slash) {
        String path = pathTempContractFolder(sessionId, slash);
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        return path;
    }

    public static boolean deleteContractDocumentFolder(Object sessionId) {
        File dir = new File(pathContractDocumentFolder(sessionId, false));
        return deleteFileDirectory(dir);
    }

    public static boolean deleteSessionFolder(Object sessionId) {
        File dir = new File(pathSessionFolder(sessionId, false));
        return deleteFileDirectory(dir);
    }

    private static boolean deleteFileDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                File childrenFile = new File(dir, aChildren);
                if (childrenFile.isDirectory()) {
                    deleteFileDirectory(childrenFile);
                }
                boolean success = childrenFile.delete();

                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        return false;
    }


}
