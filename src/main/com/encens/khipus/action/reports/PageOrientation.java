package com.encens.khipus.action.reports;

import com.jatun.titus.reportgenerator.util.ReportGeneratorConstants;

/**
 * Encens S.R.L.
 * Enumeration for report page orientation
 * Option: PORTRAIT, LANDSCAPE
 *
 * @author
 */
public enum PageOrientation {
    PORTRAIT("Reports.pageOrientation.portrait", ReportGeneratorConstants.PAGE_ORIENTATION_PORTRAIT),
    LANDSCAPE("Reports.pageOrientation.landscape", ReportGeneratorConstants.PAGE_ORIENTATION_LANDSCAPE);

    private String resourceKey;
    private String type;

    PageOrientation(String resourceKey, String type) {
        this.resourceKey = resourceKey;
        this.type = type;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getType() {
        return type;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void setType(String type) {
        this.type = type;
    }
}
