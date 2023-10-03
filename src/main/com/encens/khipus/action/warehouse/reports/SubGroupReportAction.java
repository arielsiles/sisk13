package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * Action to generate sub group report
 *
 * @author
 * @version $Id: SubGroupReportAction.java  07-may-2010 15:55:52$
 */
@Name("subGroupReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SUBGROUPREPORT','VIEW')}")
public class SubGroupReportAction extends GenericReportAction {

    private Group group;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    public void generateReport() {
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());

        super.generateReport(
                "subGroupReport",
                "/warehouse/reports/subGroupReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                MessageUtils.getMessage("Reports.subGroup.title").toUpperCase(),
                reportParameters);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "groupItem.groupCode," +
                "groupItem.name," +
                "subGroup.subGroupCode," +
                "subGroup.name" +
                " FROM Group groupItem" +
                " LEFT JOIN groupItem.subGroupList subGroup";
    }

    @Create
    public void init() {
        restrictions = new String[]{"groupItem=#{subGroupReportAction.group}"};
        sortProperty = "groupItem.groupCode,subGroup.subGroupCode";
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (null != group) {
            this.group = getEntityManager().find(Group.class, group.getId());
        } else {
            this.group = null;
        }
    }

    public void assignGroup(Group group) {
        setGroup(group);
    }

    public void cleanGroupField() {
        setGroup(null);
    }
}
