package com.encens.khipus.action.xproduction;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;

@Name("dailyProductionReportAction")
@Scope(ScopeType.PAGE)
public class DailyProductionReportAction extends GenericReportAction {

    @Create
    public void init() {
        restrictions = new String[]{""};
        sortProperty = "";

        groupByProperty = "";
    }

    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "costCenterGroup.id, " +
                "costCenterGroup.description, " +
                "costCenter.id, " +
                "costCenter.description, " +
                "jobCategory.id, " +
                "jobCategory.acronym, " +
                "businessUnit.id, " +
                "COUNT(employee.id), " +
                "businessUnit.publicity " +
                "from JobContract jobContract " +
                "     join jobContract.job job " +
                "     join job.organizationalUnit organizationalUnit " +
                "     join organizationalUnit.costCenter costCenter " +
                "     join costCenter.costCenterGroup costCenterGroup " +
                "     join job.jobCategory jobCategory " +
                "     join organizationalUnit.businessUnit businessUnit" +
                "     join jobContract.contract contract" +
                "     join contract.employee employee " +
                "     join contract.cycle cycle " +
                "     join cycle.gestion gestion ";
    }

    public void generateReport() {
        log.debug("generating DailyProductionReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "personByAreaReport",
                "/xproduction/reports/dailyProductionReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("Reports.production.dailyProductionReport"),
                reportParameters);
    }
}
