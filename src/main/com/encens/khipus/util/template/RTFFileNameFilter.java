package com.encens.khipus.util.template;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Encens S.R.L.
 * Class to filter rtf documents files
 * @author
 * @version $Id: RTFFileNameFilter.java  20-may-2010 16:22:38$
 */
public class RTFFileNameFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        name = name.toLowerCase();
        return name.endsWith(".rtf");
    }
}