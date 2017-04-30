package com.encens.khipus.model.contacts;

/**
 * Encens S.R.L.
 * File format enum
 *
 * @author
 * @version $Id: DocumentType.java  24-feb-2010 15:13:20$
 */
public enum FileFormat {
    RTF("rtf", "FileFormat.rtf");

    private String ext;
    private String resourceKey;

    FileFormat(String ext, String resourceKey) {
        this.ext = ext;
        this.resourceKey = resourceKey;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
