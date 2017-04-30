package com.encens.khipus.action.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryResult;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.reports.QueryReportUtil;
import com.encens.khipus.service.common.GenericReportService;
import com.encens.khipus.util.JSFUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class contains basic methods to execute a report
 *
 * @author
 */
@Name("genericReportAction")
@Scope(ScopeType.PAGE)
public class GenericReportAction implements Serializable {
    @Logger
    protected Log log;

    @In("#{entityManager}")
    protected EntityManager em;

    @In
    protected Map<String, String> messages;

    @In
    protected GenericReportService genericReportService;

    private String reportTitle;

    private ReportFormat reportFormat;

    private PageFormat pageFormat;

    private EntityQuery entityQuery;

    protected String sortProperty;

    protected String groupByProperty;

    private String currentSortProperties = null;

    protected boolean sortAsc = true;

    protected String[] restrictions = new String[]{};

    /**
     * Init the current EntityQuery that will be used
     * for report generation.
     *
     * @param reportId The report id
     */
    private void initEntityQuery(String reportId) {
        if (entityQuery == null) {
            entityQuery = new EntityQuery(reportId);
        }
        entityQuery.setEjbql(getEjbql());
        entityQuery.setRestrictionExpressionStrings(getRestrictions());
        entityQuery.setOrder(getOrder());
        entityQuery.setGroupBy(getGroupBy());
        entityQuery.setEntityManager(getEntityManager());

    }

    /**
     * Create a QueryResult with ejql and parameters that were prepared
     * for use on titus.
     *
     * @param reportId The report id
     * @return The query result
     */
    public QueryResult createReportQueryResult(String reportId) {
        initEntityQuery(reportId);
        return QueryReportUtil.convertToReportQuery(entityQuery.createQueryResult());
    }

    protected QueryResult createQueryForSubreport(String reportId, String ejbql, List<String> restrictions, String orderSection) {
        EntityQuery entityQueryForSubreport = new EntityQuery(reportId);
        entityQueryForSubreport.setEjbql(ejbql);
        entityQueryForSubreport.setRestrictionExpressionStrings(restrictions);
        entityQueryForSubreport.setOrder(orderSection);
        entityQueryForSubreport.setEntityManager(getEntityManager());
        return (QueryReportUtil.convertToReportQuery(entityQueryForSubreport.createQueryResult()));
    }

    protected QueryResult createQueryForSubreport(String reportId, String ejbql, List<String> restrictions, String orderSection, String groupBySection) {
        EntityQuery entityQueryForSubreport = new EntityQuery(reportId);
        entityQueryForSubreport.setEjbql(ejbql);
        entityQueryForSubreport.setRestrictionExpressionStrings(restrictions);
        entityQueryForSubreport.setOrder(orderSection);
        entityQueryForSubreport.setGroupBy(groupBySection);
        entityQueryForSubreport.setEntityManager(getEntityManager());
        return (QueryReportUtil.convertToReportQuery(entityQueryForSubreport.createQueryResult()));
    }

    protected List<String> getRestrictions() {
        return Arrays.asList(restrictions);
    }

    protected String getEjbql() {
        return null;
    }

    private String getOrder() {
        if (sortProperty != null) {
            String currentOrder = sortAsc ? " ASC" : " DESC";
            currentSortProperties = sortProperty.replaceAll(" ", "").replaceAll(",", currentOrder + ",") + currentOrder;
        }
        return currentSortProperties;
    }

    private String getGroupBy() {
        return groupByProperty;
    }

    protected String getNativeSql() {
        return null;
    }

    public void generateReportFromNativeSql(String reportId,
                                            String reportTemplatePath,
                                            PageFormat pageFormat,
                                            PageOrientation pageOrientation,
                                            String defaultReportTitle) {
        String sql = getNativeSql();
        log.debug("-> The native sql is : " + sql);
    }


    /**
     * Generate's a report
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param defaultReportTitle A default title
     * @param params             A map of params
     */
    public void generateReport(String reportId, String reportTemplatePath,
                               PageFormat pageFormat,
                               PageOrientation pageOrientation,
                               String defaultReportTitle, Map params) {
        log.debug("Generating report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }
        QueryResult queryResult = createReportQueryResult(reportId);

        params.putAll(queryResult.getQueryParameters());

        try {
            genericReportService.generateReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    pageFormat.getType(),
                    pageOrientation.getType(),
                    defaultReportTitle,
                    queryResult.getEjbql(),
                    reportTitleString).exportReport();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
    }

    /**
     * Generate's a report using template page sizes and orientation
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param defaultReportTitle A default title
     * @param params             A map of params
     */
    public void generateReport(String reportId, String reportTemplatePath,
                               String defaultReportTitle, Map params) {
        log.debug("Generating report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }
        QueryResult queryResult = createReportQueryResult(reportId);

        params.putAll(queryResult.getQueryParameters());
        try {
            genericReportService.generateReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    defaultReportTitle,
                    queryResult.getEjbql(),
                    reportTitleString).exportReport();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
    }

    /**
     * This method generates a subreport
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param queryResult        The generated Query Result
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData getReport(String reportId, String reportTemplatePath, PageFormat pageFormat, PageOrientation pageOrientation,
                                             QueryResult queryResult,String defaultReportTitle, Map params) {

        TypedReportData typedReportData = new TypedReportData();
        log.debug("Generating report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }

        params.putAll(queryResult.getQueryParameters());
        try {
            typedReportData = genericReportService.generateReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    pageFormat.getType(),
                    pageOrientation.getType(),
                    defaultReportTitle,
                    queryResult.getEjbql(),
                    reportTitleString).getTypedReportData();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
        return typedReportData;
    }

    /**
     * Generate's a report using template page sizes and orientation
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param defaultReportTitle A default title
     * @param params             A map of params
     */
    public TypedReportData getReport(String reportId, String reportTemplatePath,
                                     String defaultReportTitle, Map params) {
        TypedReportData typedReportData = new TypedReportData();
        log.debug("Generating report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }
        QueryResult queryResult = createReportQueryResult(reportId);

        params.putAll(queryResult.getQueryParameters());
        try {
            typedReportData = genericReportService.generateReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    defaultReportTitle,
                    queryResult.getEjbql(),
                    reportTitleString).getTypedReportData();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
        return typedReportData;
    }

    public TypedReportData getReport(String reportId, String reportTemplatePath, String query, Map params, String defaultReportTitle) {

        TypedReportData typedReportData = new TypedReportData();
        log.debug("Generating sql report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }
        try {
            typedReportData = genericReportService.generateSqlReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    defaultReportTitle,
                    query,
                    reportTitleString).getTypedReportData();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }

        return typedReportData;
    }

    /**
     * This method generates a subreport
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSubReport(String reportId, String reportTemplatePath, PageFormat pageFormat, PageOrientation pageOrientation, Map params) {
        log.debug("Generating SubReport........................");

        TypedReportData typedReportData = null;

        try {
            typedReportData = genericReportService.generateSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    pageOrientation.getType(),
                    pageFormat.getType());
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    /**
     * This method generates a subreport
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param queryResult        The generated Query Result
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSubReport(String reportId, String reportTemplatePath, PageFormat pageFormat, PageOrientation pageOrientation,
                                             QueryResult queryResult, Map params) {
        log.debug("Generating SubReport........................");
        params.putAll(queryResult.getQueryParameters());

        TypedReportData typedReportData = null;

        try {
            typedReportData = genericReportService.generateSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    pageOrientation.getType(),
                    pageFormat.getType(),
                    queryResult.getEjbql());
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    /**
     * This method generates a subreport using template page sizes and orientation
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSubReport(String reportId, String reportTemplatePath, Map params) {
        log.debug("Generating SubReport........................");
        TypedReportData typedReportData = null;
        try {
            typedReportData = genericReportService.generateSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat());
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    /**
     * This method generates a subreport using template page sizes and orientation
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param queryResult        The generated Query Result
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSubReport(String reportId, String reportTemplatePath,
                                             QueryResult queryResult, Map params) {
        log.debug("Generating SubReport........................");
        params.putAll(queryResult.getQueryParameters());

        TypedReportData typedReportData = null;

        try {
            typedReportData = genericReportService.generateSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    queryResult.getEjbql());
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    /**
     * Generate's a sql report
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param defaultReportTitle A default title
     * @param params             A map of params
     */
    public void generateSqlReport(String reportId, String reportTemplatePath,
                                  PageFormat pageFormat,
                                  PageOrientation pageOrientation,
                                  String defaultReportTitle, Map params) {
        log.debug("Generating sql report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }

        TypedReportData typedReportData = null;
        GenerationReportData generationReportData = GenerationReportData.getInstance("messages_app",
                (SessionUser) Component.getInstance("sessionUser"),
                params);
        String reportFormat = getReportFormat().getFormat();
        String typeReport = pageFormat.getType();
        String pageOrient = pageOrientation.getType();
        String sql = "";
        try {
            genericReportService.generateSqlReport(
                    generationReportData,
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    reportFormat,
                    pageFormat.getType(),
                    pageOrientation.getType(),
                    defaultReportTitle,
                    sql,
                    reportTitleString).exportReport();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
    }

    /**
     * Generate's a report using template page sizes and orientation (using sql query)
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param defaultReportTitle A default title
     * @param params             A map of params
     */
    public void generateSqlReport(String reportId, String reportTemplatePath,
                                  String defaultReportTitle, Map params) {
        log.debug("Generating sql report........................");
        String reportTitleString = getReportTitle();
        if (reportTitleString == null || reportTitleString.isEmpty()) {
            reportTitleString = defaultReportTitle;
        }

        try {
            genericReportService.generateSqlReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    defaultReportTitle,
                    getNativeSql(),
                    reportTitleString).exportReport();
        } catch (IOException e) {
            log.error("ERROR IN GENERATING REPORT......................" + e.getMessage());
        }
    }

    /**
     * This method generates a subreport (using sql query)
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param pageFormat         The page size
     * @param pageOrientation    The page orientation
     * @param sqlQuery           The sql Query
     * @param params             A map with reportParams
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSqlSubReport(String reportId, String reportTemplatePath, PageFormat pageFormat, PageOrientation pageOrientation,
                                                String sqlQuery, Map params) {
        log.debug("Generating sql SubReport........................");

        TypedReportData typedReportData = null;

        try {
            typedReportData = genericReportService.generateSqlSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    pageOrientation.getType(),
                    pageFormat.getType(),
                    sqlQuery);
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SQL SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    /**
     * This method generates a subreport using template page sizes and orientation (using sql query)
     *
     * @param reportId           ReportId
     * @param reportTemplatePath Report template path
     * @param params             A map with reportParams
     * @param sqlQuery           The sql query
     * @return A TypedReportData with a JasperReport (a compiled report)
     */
    public TypedReportData generateSqlSubReport(String reportId, String reportTemplatePath,
                                                String sqlQuery, Map params) {
        log.debug("Generating SubReport........................");

        TypedReportData typedReportData = null;

        try {
            typedReportData = genericReportService.generateSqlSubReport(
                    GenerationReportData.getInstance("messages_app",
                            (SessionUser) Component.getInstance("sessionUser"),
                            params),
                    reportId,
                    JSFUtil.getResourceAsStream(reportTemplatePath),
                    getReportFormat().getFormat(),
                    sqlQuery);
        } catch (IOException e) {
            log.error("ERROR IN GENERATING SQL SUB REPORT......................" + e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return typedReportData;
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public ReportFormat[] getReportFormats() {
        return ReportFormat.values();
    }

    public PageFormat[] getPageFormats() {
        return PageFormat.values();
    }

    public ReportFormat getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(ReportFormat reportFormat) {
        this.reportFormat = reportFormat;
    }

    public PageFormat getPageFormat() {
        return pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

}
