package com.encens.khipus.service.common;

import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.dataintegration.configuration.Configuration;
import com.encens.khipus.reports.GenerationReportData;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * GenericReportServiceBean
 *
 * @since This source code was benchmarked for improve the original source code of ReportUtil class 
 * @author
 * @version 2.26
 */
@Stateless
@Name("genericReportService")
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class GenericReportServiceBean implements GenericReportService {

    @Logger
    protected Log log;

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "khipus")
    private EntityManager entityManager;

    /**
     * Generates a report and upload it to the client's browser
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param pageSize             PageSize
     * @param pageOrientation      PageOrientation
     * @param fileName             FileName (for exporting the report)
     * @param jpqlQuery            The jpql query
     * @param reportTitle          The report title
     * @throws IOException An exception can be thrown
     */
    public synchronized GenerationReportData generateReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageSize,
                                                            String pageOrientation, String fileName, String jpqlQuery, String reportTitle) throws IOException {
        log.debug("Generating report........\n reportId: " + reportId + " templateInputStream: " + templateInputStream + " reportFormat: " + reportFormat +
                " pageSize: " + pageSize + " pageOrientation: " + pageOrientation + " fileName: " + fileName + " jpqlQuery: " + jpqlQuery +
                " reportTitle: " + reportTitle);
        generationReportData.getReportParams().put("reportTitleParam", reportTitle);

        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageOrientation(pageOrientation);
        generationReportData.getReportConfigParams().setReportPageSize(pageSize);
        generationReportData.getReportConfigParams().setReportFileName(fileName);
        generationReportData.getReportConfigParams().setJPQLQuery(jpqlQuery);

        generateForJpql(generationReportData);

        return generationReportData;
    }

    /**
     * Generates a report and upload it to the client's browser, this method use the template page sizes and page orientation
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param fileName             FileName (for exporting the report)
     * @param jpqlQuery            The jpql query
     * @param reportTitle          The report title
     * @throws IOException An exception can be thrown
     */
    public synchronized GenerationReportData generateReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                                            String fileName, String jpqlQuery, String reportTitle) throws IOException {
        log.debug("Generating report........\n reportId: " + reportId + " templateInputStream: " + templateInputStream + " reportFormat: " + reportFormat +
                " fileName: " + fileName + " jpqlQuery: " + jpqlQuery +
                " reportTitle: " + reportTitle);
        return generateReport(generationReportData, reportId, templateInputStream, reportFormat, null, null, fileName, jpqlQuery, reportTitle);
    }

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param pageOrientation      PageOrientation
     * @param pageSize             PageSize
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                                          String pageSize) throws IOException {

        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageOrientation(pageOrientation);
        generationReportData.getReportConfigParams().setReportPageSize(pageSize);
        generationReportData.getReportConfigParams().setOnlyCompile(true);

        generateForJpql(generationReportData);
        return generationReportData.getTypedReportData();
    }

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param pageOrientation      PageOrientation
     * @param pageSize             PageSize
     * @param jpqlQuery            jpqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                                          String pageSize, String jpqlQuery) throws IOException {
        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageOrientation(pageOrientation);
        generationReportData.getReportConfigParams().setReportPageSize(pageSize);
        generationReportData.getReportConfigParams().setOnlyCompile(true);
        generationReportData.getReportConfigParams().setJPQLQuery(jpqlQuery);

        generateForJpql(generationReportData);
        return generationReportData.getTypedReportData();
    }

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param jpqlQuery            jpqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                                          String jpqlQuery) throws IOException {
        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setOnlyCompile(true);
        generationReportData.getReportConfigParams().setJPQLQuery(jpqlQuery);

        generateForJpql(generationReportData);
        return generationReportData.getTypedReportData();
    }

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat) throws IOException {
        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setOnlyCompile(true);
        generationReportData.getReportConfigParams().setReportPageSize(PageFormat.CUSTOM.getType());

        generateForJpql(generationReportData);
        return generationReportData.getTypedReportData();
    }


    /**
     * Generates a report and upload it to the client's browser (using a sql query)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param pageSize             PageSize
     * @param pageOrientation      PageOrientation
     * @param fileName             FileName (for exporting the report)
     * @param sqlQuery             The sql query
     * @param reportTitle          The report title
     * @throws IOException An exception can be thrown
     */
    public synchronized GenerationReportData generateSqlReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageSize,
                                                               String pageOrientation, String fileName, String sqlQuery, String reportTitle) throws IOException {
        log.debug("Generating report........\n reportId: " + reportId + " templateInputStream: " + templateInputStream + " reportFormat: " + reportFormat +
                " pageSize: " + pageSize + " pageOrientation: " + pageOrientation + " fileName: " + fileName + " sqlQuery: " + sqlQuery +
                " reportTitle: " + reportTitle);

        generationReportData.getReportParams().put("reportTitleParam", reportTitle);
        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageOrientation(pageOrientation);
        generationReportData.getReportConfigParams().setReportPageSize(pageSize);
        generationReportData.getReportConfigParams().setReportFileName(fileName);
        generationReportData.getReportConfigParams().setSQLQuery(sqlQuery);

        generateForSql(generationReportData);
        return generationReportData;
    }

    /**
     * Generates a report and upload it to the client's browser, this method use the template page sizes and page orientation (using a sql query)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param fileName             FileName (for exporting the report)
     * @param sqlQuery             The jpql query
     * @param reportTitle          The report title
     * @throws IOException An exception can be thrown
     */
    public synchronized GenerationReportData generateSqlReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                                               String fileName, String sqlQuery, String reportTitle) throws IOException {
        log.debug("Generating report........\n reportId: " + reportId + " templateInputStream: " + templateInputStream + " reportFormat: " + reportFormat +
                " fileName: " + fileName + " sqlQuery: " + sqlQuery +
                " reportTitle: " + reportTitle);
        return generateSqlReport(generationReportData, reportId, templateInputStream, reportFormat, null, null, fileName, sqlQuery, reportTitle);
    }

    /**
     * Generates a subreport (only compile a report)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param pageOrientation      PageOrientation
     * @param pageSize             PageSize
     * @param sqlQuery             The Sql Query
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSqlSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat, String pageOrientation,
                                                             String pageSize, String sqlQuery) throws IOException {
        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageOrientation(pageOrientation);
        generationReportData.getReportConfigParams().setReportPageSize(pageSize);
        generationReportData.getReportConfigParams().setOnlyCompile(true);
        generationReportData.getReportConfigParams().setSQLQuery(sqlQuery);

        generateForSql(generationReportData);
        return generationReportData.getTypedReportData();
    }

    /**
     * Generates a subreport (only compile a report) using template page sizes and orientation (and using an sql query)
     *
     * @param generationReportData generationReportData
     * @param reportId             reportId
     * @param templateInputStream  Template (as inputStream)
     * @param reportFormat         ReportFormat
     * @param sqlQuery             sqlQuery
     * @return a TypedReportData that contains a JasperReport (a compiled version if the report)
     * @throws IOException An exception can be thrown
     */
    public synchronized TypedReportData generateSqlSubReport(GenerationReportData generationReportData, String reportId, InputStream templateInputStream, String reportFormat,
                                                             String sqlQuery) throws IOException {

        generationReportData.getReportConfigParams().setReportId(reportId);
        generationReportData.getReportConfigParams().setTemplateInputStream(templateInputStream);
        generationReportData.getReportConfigParams().setReportFormat(reportFormat);
        generationReportData.getReportConfigParams().setReportPageSize(PageFormat.CUSTOM.getType());
        generationReportData.getReportConfigParams().setOnlyCompile(true);
        generationReportData.getReportConfigParams().setJPQLQuery(sqlQuery);

        generateForSql(generationReportData);
        return generationReportData.getTypedReportData();
    }

    private Connection getDbConnection() {
        String dataSourceJNDI = Configuration.i.getLocalDataSource();
        try {
            Context context = new javax.naming.InitialContext();
            DataSource dataSource = (DataSource) context.lookup(dataSourceJNDI);
            if (dataSource != null) {
                try {
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    log.error("Error when setting datasource...", e);
                }
            }
        } catch (NamingException e) {
            log.error("Cannot find the dataSource " + dataSourceJNDI);
        }
        return null;
    }

    public void generateForJpql(GenerationReportData generationReportData) {
        if (generationReportData.isEntityManagerEmpty()) {
            generationReportData.setEntityManager(entityManager);
        }
        generationReportData.generateReport();
    }

    public void generateForSql(GenerationReportData generationReportData) {
        if (generationReportData.isDbConnectionEmpty()) {
            generationReportData.setDbConnection(getDbConnection());
        }
        generationReportData.generateReport();
    }
}
