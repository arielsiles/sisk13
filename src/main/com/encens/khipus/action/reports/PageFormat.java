package com.encens.khipus.action.reports;

import com.jatun.titus.reportgenerator.util.ReportGeneratorConstants;

/**
 * Constants for report page formats, this constants are related with Titus constants.
 * <p/>
 * The available constants are a4, letter, legal and custom.
 *
 * @author
 * @version 1.0.18
 */
public enum PageFormat {
    A4("Reports.page.aFour", ReportGeneratorConstants.PAGE_A4),
    LETTER("Reports.page.letter", ReportGeneratorConstants.PAGE_LETTER),
    LEGAL("Reports.page.legal", ReportGeneratorConstants.PAGE_LEGAL),
    CUSTOM("Reports.page.custom", ReportGeneratorConstants.PAGE_CUSTOM);

    private String resourceKey;
    private String type;

    PageFormat(String resourceKey, String type) {
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
