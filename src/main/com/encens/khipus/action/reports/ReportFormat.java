package com.encens.khipus.action.reports;

import com.jatun.titus.reportgenerator.util.ReportGeneratorConstants;

/**
 * Constants for report file format, this constants are related with Titus constants.
 * <p/>
 * The available constants are  pdf, xls, csv and rtf.
 *
 * @author
 * @version 1.0.18
 */
public enum ReportFormat {
    PDF("Reports.format.pdf", ReportGeneratorConstants.REPORT_FORMAT_PDF),
    XLS("Reports.format.xls", ReportGeneratorConstants.REPORT_FORMAT_XLS),
    CSV("Reports.format.csv", ReportGeneratorConstants.REPORT_FORMAT_CSV),
    RTF("Reports.format.rtf", ReportGeneratorConstants.REPORT_FORMAT_RTF),
    XLSX("Reports.format.xlsx", ReportGeneratorConstants.REPORT_FORMAT_XLSX);

    private String resourceKey;
    private String format;

    ReportFormat(String resourceKey, String format) {
        this.resourceKey = resourceKey;
        this.format = format;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getFormat() {
        return format;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
