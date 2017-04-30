package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.Gestion;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the person by area report
 *
 * @author
 * @version 2.1.2
 */
@Name("personByAreaReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PERSONBYAREAREPORT','VIEW')}")
public class PersonByAreaReportAction extends GenericReportAction {
    private Gestion gestion;

    @Create
    public void init() {
        restrictions = new String[]{"gestion=#{personByAreaReportAction.gestion}"};
        sortProperty = "costCenterGroup.id, " +
                "costCenterGroup.description, " +
                "costCenter.id, " +
                "costCenter.description, " +
                "jobCategory.id, " +
                "jobCategory.acronym, " +
                "businessUnit.id, " +
                "businessUnit.publicity";

        groupByProperty = "costCenterGroup.id, " +
                "costCenterGroup.description, " +
                "costCenter.id, " +
                "costCenter.description, " +
                "jobCategory.id, " +
                "jobCategory.acronym, " +
                "businessUnit.id, " +
                "businessUnit.publicity";
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
        log.debug("generating personByAreaReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "personByAreaReport",
                "/employees/reports/personByAreaReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("PersonByAreaReport.report"),
                reportParameters);
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }
}