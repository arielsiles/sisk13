package com.encens.khipus.reports;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.util.FileCacheLoader;
import com.encens.khipus.util.JSFUtil;
import com.jatun.titus.reportgenerator.TemplateReportGenerator;
import com.jatun.titus.reportgenerator.util.ReportConfigParams;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import com.jatun.titus.web.util.SubReportInfo;
import net.sf.jasperreports.engine.JasperPrint;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * GenerationReportData
 *
 * @author
 * @version 2.26
 */
public class GenerationReportData implements Serializable {
    private TemplateReportGenerator templateReportGenerator;
    private TypedReportData typedReportData;

    public static GenerationReportData getInstance(String resourceBundle, SessionUser sessionUser, Map params) {
        return new GenerationReportData(resourceBundle, sessionUser, params);
    }

    /**
     * This constructor initialize some required common values for the report generation
     *
     * @param resourceBundle The resource bundle
     * @param sessionUser    The session user
     * @param params         A map with params
     * @return
     */
    private GenerationReportData(String resourceBundle, SessionUser sessionUser, Map params) {
        // init typedReportData
        typedReportData = new TypedReportData();

        typedReportData.getReportConfigParams().setResourceBundle(resourceBundle);
        typedReportData.getReportConfigParams().setReportLocale(sessionUser.getLocale());
        typedReportData.getReportConfigParams().setReportTempDirectory(System.getProperty("java.io.tmpdir"));
        typedReportData.getReportParams().put("REPORT_LOCALE", sessionUser.getLocale());
        typedReportData.getReportParams().put("REPORT_TIMEZONE", sessionUser.getTimeZone());

        //Add report params
        typedReportData.getReportParams().putAll(params);

        // init templateReportGenerator
        templateReportGenerator = new TemplateReportGenerator();

        FileCacheLoader.i.refreshRoot();
    }

    public GenerationReportData(TypedReportData reportData) {
        // init typedReportData
        typedReportData = reportData;

        // init templateReportGenerator
        templateReportGenerator = new TemplateReportGenerator();

        FileCacheLoader.i.refreshRoot();
    }

    public TemplateReportGenerator getTemplateReportGenerator() {
        return templateReportGenerator;
    }

    public TypedReportData getTypedReportData() {
        return typedReportData;
    }

    public Boolean isEntityManagerEmpty() {
        return typedReportData.getReportConfigParams().getEntityManager() == null;
    }

    public GenerationReportData setEntityManager(EntityManager entityManager) {
        typedReportData.getReportConfigParams().setEntityManager(entityManager);
        return this;
    }

    public Boolean isDbConnectionEmpty() {
        return typedReportData.getReportConfigParams().getDbConnection() == null;
    }

    public GenerationReportData setDbConnection(Connection dbConnection) {
        typedReportData.getReportConfigParams().setDbConnection(dbConnection);
        return this;
    }

    public ReportConfigParams getReportConfigParams() {
        return typedReportData.getReportConfigParams();
    }

    public HashMap getReportParams() {
        return typedReportData.getReportParams();
    }

    /**
     * Execute only on business layer
     */

    public void generateReport() {
        typedReportData = templateReportGenerator.generateReport(typedReportData);
    }

    /**
     * Execute only on web layer
     */

    public void exportReport() throws IOException {
        HttpServletResponse httpServletResponse = JSFUtil.getHttpServletResponse();
        templateReportGenerator.exportReport(typedReportData, httpServletResponse);
        httpServletResponse.flushBuffer();
        JSFUtil.getFacesContext().responseComplete();
    }

    public TypedReportData getExportReport() {
        return this.typedReportData;
    }
}
