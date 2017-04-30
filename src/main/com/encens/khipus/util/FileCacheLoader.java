package com.encens.khipus.util;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * FileCacheLoader
 *
 * @author
 * @version 2.26
 */
public class FileCacheLoader {
    private static transient LogProvider log = Logging.getLogProvider(FileCacheLoader.class);
    private static final String ROOT_RELATIVE_PATH = "/";
    private final Map<String, String> absolutePathCache;
    public static final FileCacheLoader i = new FileCacheLoader();

    private FileCacheLoader() {
        absolutePathCache = new HashMap<String, String>();
    }

    public synchronized String refreshRoot() {
        if (!FileUtil.isValidFile(absolutePathCache.get(ROOT_RELATIVE_PATH))) {
            File rootFile = new File(JSFUtil.getRealPath(ROOT_RELATIVE_PATH));
            absolutePathCache.put(ROOT_RELATIVE_PATH, rootFile.getAbsolutePath());
        }
        return absolutePathCache.get(ROOT_RELATIVE_PATH);
    }

    /**
     * This method the absolute path of relative path sent of  relativePath parameter.
     *
     * @param relativePath The relative path(web layer). E.g. /include/reports/reportStyles.jrtx
     * @return The absolute path of relative path
     */
    public synchronized String getPath(String relativePath) {
        if (!FileUtil.isValidFile(absolutePathCache.get(relativePath))) {
            String rootPath = refreshRoot();
            File absoluteFile = new File(rootPath + File.separator + relativePath);
            absolutePathCache.put(relativePath, absoluteFile.getAbsolutePath());
            log.debug("RELATIVE_PART = " + relativePath + " ABSOLUTE_PATH = " + (absoluteFile.getAbsolutePath()));
        } else {
            log.debug("RELATIVE_PART = " + relativePath + " ABSOLUTE_PATH = " + absolutePathCache.get(relativePath));
        }

        return absolutePathCache.get(relativePath);
    }
}
