package com.encens.khipus.action.xproduction;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;
import java.util.HashMap;

@Name("dailyProductionReportAction")
@Scope(ScopeType.PAGE)
public class DailyProductionReportAction extends GenericReportAction {

    private Date initDate;
    private Date endDate;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @Create
    public void init() {
        restrictions = new String[]{
                "plan.date >= #{dailyProductionReportAction.initDate}",
                "plan.date <= #{dailyProductionReportAction.endDate}"
        };

        sortProperty = "plan.date";

        /*groupByProperty = "";*/
    }

    protected String getEjbql() {
        return "SELECT " +
                "plan.date, " +
                "production.code as prodCode, " +
                "production.shiftType as shiftType, " +
                "productionGroup.code as groupCode, " +
                "productItem.productItemCode as codeArt, " +
                "productItem.name, " +
                "(product.quantity / 1000) as quantity, " +
                "(production.totalRawMaterial / 1000) as totalmp " +
                "from XProductionProduct product " +
                "     join product.production production " +
                "     join production.productionPlan plan " +
                "     join product.productItem productItem " +
                "     join production.productionGroup productionGroup "
                ;
    }

    public void generateReport() {
        log.debug("generating DailyProductionReport......................................");

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");}

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("reportTitle", "REPORTE DE PRODUCCION DIARIA");
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("initDate", initDate);
        reportParameters.put("endDate", endDate);

        super.generateReport(
                "dailyProductionReport",
                "/xproduction/reports/productionReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.production.dailyProductionReport"),
                reportParameters);
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
