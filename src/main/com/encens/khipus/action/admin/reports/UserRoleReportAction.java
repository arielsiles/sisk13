package com.encens.khipus.action.admin.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.UserRoleGroupingType;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * UserRoleReportAction
 *
 * @author
 * @version 2.26
 */
@Name("userRoleReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('USERROLEREPORT','VIEW')}")
public class UserRoleReportAction extends GenericReportAction {

    private UserRoleGroupingType groupingType;

    public void generateReport() {
        log.debug("Generate userRoleReportAction......");
        //set filter properties

        Map params = new HashMap();

        putDataByType(params);

        super.generateReport("userRoleReport",
                "/admin/reports/userRoleReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                MessageUtils.getMessage(getFileName()), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                getSelectProjection() +
                " FROM User user" +
                " LEFT JOIN user.employee employee" +
                " LEFT JOIN user.roles role";
    }

    @Create
    public void init() {
        restrictions = new String[]{"user.company=#{currentCompany}"
        };
    }

    private void putDataByType(Map params) {
        if (UserRoleGroupingType.GROUPING_BY_USER.equals(getGroupingType())) {
            params.put("REPORT_TITLE", MessageUtils.getMessage("Reports.userRole.byUser"));
            params.put("HEADER_TITLE", MessageUtils.getMessage("Reports.userRole.userName"));
            params.put("FIRST_COLUMN_TITLE", MessageUtils.getMessage("Reports.userRole.roleName"));
            params.put("SECOND_COLUMN_TITLE", MessageUtils.getMessage("Reports.userRole.roleDescription"));

            sortProperty = "user.username, employee.lastName, employee.maidenName, employee.firstName, role.name";
        } else if (UserRoleGroupingType.GROUPING_BY_ROLE.equals(getGroupingType())) {
            params.put("REPORT_TITLE", MessageUtils.getMessage("Reports.userRole.byRole"));
            params.put("HEADER_TITLE", MessageUtils.getMessage("Reports.userRole.roleName"));
            params.put("FIRST_COLUMN_TITLE", MessageUtils.getMessage("Reports.userRole.userAcccount"));
            params.put("SECOND_COLUMN_TITLE", MessageUtils.getMessage("Reports.userRole.userName"));

            sortProperty = "role.name, user.username, employee.lastName, employee.maidenName, employee.firstName";
        }
    }

    public String getSelectProjection() {
        if (UserRoleGroupingType.GROUPING_BY_USER.equals(getGroupingType())) {
            return "user.id," +
                    "concat(concat(concat(concat(concat(concat(concat(user.username,' ( ') , employee.lastName),' '),employee.maidenName),' '),employee.firstName),' )')," +
                    "role.name," +
                    "role.description";
        } else if (UserRoleGroupingType.GROUPING_BY_ROLE.equals(getGroupingType())) {
            return "role.id," +
                    "role.name," +
                    "user.username," +
                    "concat(concat(concat(concat(employee.lastName,' '),employee.maidenName),' '),employee.firstName)";
        }
        return null;
    }

    public String getFileName() {
        return (UserRoleGroupingType.GROUPING_BY_USER.equals(getGroupingType())) ? "Reports.userRole.byUser" :
                (UserRoleGroupingType.GROUPING_BY_ROLE.equals(getGroupingType())) ? "Reports.userRole.byRole" :
                        "Reports.userRole.fileName";
    }

    public UserRoleGroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(UserRoleGroupingType groupingType) {
        this.groupingType = groupingType;
    }
}
