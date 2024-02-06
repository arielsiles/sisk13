package com.encens.khipus.action.xproduction.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.warehouse.SubGroupPK;
import com.encens.khipus.model.warehouse.SubGroupState;
import com.encens.khipus.service.production.ProductionPlanningService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("rawMilkReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTMILKRAW','VIEW')}")
public class RawMilkReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private List<SubGroupPK> subGroups = new ArrayList<SubGroupPK>();
    private SubGroupState state;

    @In
    private ProductionPlanningService productionPlanningService;


    @Create
    public void init() {

        /*this.subGroups.add(new SubGroupPK("01","7","4"));
        this.subGroups.add(new SubGroupPK("01","7","1"));
        this.subGroups.add(new SubGroupPK("01","7","2"));
        this.subGroups.add(new SubGroupPK("01","8","1"));
        this.subGroups.add(new SubGroupPK("01","8","2"));
        this.subGroups.add(new SubGroupPK("01","8","3"));
        this.subGroups.add(new SubGroupPK("01","8","4"));
        this.subGroups.add(new SubGroupPK("01","9","1"));*/

        this.subGroups.add(new SubGroupPK("01","4","1"));
        this.subGroups.add(new SubGroupPK("01","4","2"));
        this.subGroups.add(new SubGroupPK("01","5","1"));
        this.subGroups.add(new SubGroupPK("01","5","2"));
        this.subGroups.add(new SubGroupPK("01","5","3"));
        this.subGroups.add(new SubGroupPK("01","5","4"));
        this.subGroups.add(new SubGroupPK("01","6","1"));


        restrictions = new String[]{
                 "subGroup.id in (#{rawMilkReportAction.subGroups})"
        };
        sortProperty = "subGroup.name";
    }

    @Override
    protected String getEjbql() {
        return  " SELECT subGroup.name " +
                " ,subGroup.groupCode " +
                " ,subGroup.subGroupCode " +
                "FROM  SubGroup subGroup ";

    }

    public void generateReport() {
        log.debug("Generating raw milk used Stock report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("startDate",startDate);
        reportParameters.put("endDate",endDate);
        reportParameters.put("total",productionPlanningService.getTotalMilkByDate(startDate,endDate));
        super.generateReport(
                "rawMilkReport",
                "/production/reports/rawMilkReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("production.rawMilkReports.TitleReport"),
                reportParameters);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<SubGroupPK> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<SubGroupPK> subGroups) {
        this.subGroups = subGroups;
    }

    public SubGroupState getState() {
        return state;
    }

    public void setState(SubGroupState state) {
        this.state = state;
    }
}
