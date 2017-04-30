package com.encens.khipus.service.common;

import com.encens.khipus.reports.GenerationReportData;
import com.jatun.titus.reportgenerator.util.TypedReportData;

import javax.ejb.Local;
import java.io.IOException;
import java.io.InputStream;

/**
 * GenericReportService
 *
 * @author
 * @version 2.26
 */
@Local
public interface GenericReportService {
   
    /**
     * Generates a report and upload it to the client's browser
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param pageSize            PageSize
     * @param pageOrientation     PageOrientation
     * @param fileName            FileName (for exporting the report)
     * @param jpqlQuery           The jpql query
     * @param reportTitle         The report title
     * @throws java.io.IOException An exception can be thrown
     */
    GenerationReportData generateReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageSize,
                        String pageOrientation, String fileName, String jpqlQuery, String reportTitle) throws IOException;

    /**
     * Generates a report and upload it to the client's browser, this method use the template page sizes and page orientation
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param fileName            FileName (for exporting the report)
     * @param jpqlQuery           The jpql query
     * @param reportTitle         The report title
     * @throws java.io.IOException An exception can be thrown
     */
    GenerationReportData generateReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                        String fileName, String jpqlQuery, String reportTitle) throws IOException;

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param pageOrientation     PageOrientation
     * @param pageSize            PageSize
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                      String pageSize) throws IOException;

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param pageOrientation     PageOrientation
     * @param pageSize            PageSize
     * @param jpqlQuery           jpqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                      String pageSize, String jpqlQuery) throws IOException;

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param jpqlQuery           jpqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                      String jpqlQuery) throws IOException;

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat) throws IOException;

    /**
     * Generates a report and upload it to the client's browser (using a sql query)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param pageSize            PageSize
     * @param pageOrientation     PageOrientation
     * @param fileName            FileName (for exporting the report)
     * @param sqlQuery            The sql query
     * @param reportTitle         The report title
     * @throws java.io.IOException An exception can be thrown
     */
    GenerationReportData generateSqlReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageSize,
                           String pageOrientation, String fileName, String sqlQuery, String reportTitle) throws IOException;

    /**
     * Generates a report and upload it to the client's browser, this method use the template page sizes and page orientation (using a sql query)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param fileName            FileName (for exporting the report)
     * @param sqlQuery            The jpql query
     * @param reportTitle         The report title
     * @throws java.io.IOException An exception can be thrown
     */
    GenerationReportData generateSqlReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                           String fileName, String sqlQuery, String reportTitle) throws IOException;

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param pageOrientation     PageOrientation
     * @param pageSize            PageSize
     * @param sqlQuery            The Sql Query
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSqlSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                         String pageSize, String sqlQuery) throws IOException;

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation (and using an sql query)
     *
     * @param generationReportData   generationReportData
     * @param reportId            reportId
     * @param templateInputStream Template (as inputStream)
     * @param reportFormat        ReportFormat
     * @param sqlQuery            sqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws java.io.IOException An exception can be thrown
     */
    TypedReportData generateSqlSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                         String sqlQuery) throws IOException;
}
