package com.encens.khipus.util;

import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.FileFormat;

/**
 * Encens S.R.L.
 * File util
 *
 * @author
 * @version $Id: FileUtil.java  01-mar-2010 15:50:19$
 */
public final class FileUtil {

    private FileUtil() {
    }

    public static Boolean isEmpty(File file) {
        return file == null || file.getSize() == 0 || file.getValue() == null;
    }

    /**
     * Validate if this File is of this file format
     *
     * @param file
     * @param fileFormat
     * @return boolean
     */
    public static boolean isValidFileFormat(File file, FileFormat fileFormat) {
        boolean isValid = true;
        if (FileFormat.RTF.equals(fileFormat)) {
            isValid = isRtfFormat(file);
        }
        return isValid;
    }

    public static boolean isRtfFormat(File file) {
        boolean isRtf = false;
        if (!isEmpty(file) && FileFormat.RTF.getExt().compareToIgnoreCase(getFileNameExtencion(file.getName())) == 0 &&
                ("application/msword".equals(file.getContentType()) ||
                        "application/octet-stream".equals(file.getContentType()) ||
                        "text/richtext".equals(file.getContentType()))) {
            isRtf = true;
        }
        return isRtf;
    }

    public static boolean isImageFormat(File file) {
        return !isEmpty(file) && ("image/jpeg".compareToIgnoreCase(file.getContentType()) == 0 ||
                "image/gif".compareToIgnoreCase(file.getContentType()) == 0 ||
                "image/png".compareToIgnoreCase(file.getContentType()) == 0);
    }

    private static String getFileNameExtencion(String fileName) {
        String ext = null;
        if (fileName != null) {
            ext = (fileName.lastIndexOf('.') == -1) ? "" : fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
        }
        return ext;
    }

    /**
     * Validate if the current pathName is a valid file
     */
    public static Boolean isValidFile(String pathName) {
        if (ValidatorUtil.isBlankOrNull(pathName)) {
            return false;
        }
        java.io.File file = new java.io.File(pathName);
        return file.isFile() || file.isDirectory() || file.isHidden();
    }
}
