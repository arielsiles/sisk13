package com.encens.khipus.action.admin;

import com.encens.khipus.model.admin.SessionUserLog;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.List;

/**
 * Session User Log data model
 *
 * @author
 * @version 2.17
 */
@Name("sessionUserLogDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SESSIONUSERLOG','VIEW')}")
public class SessionUserLogDataModel {
    @In
    private SessionUserLogAction userSessionAction;

    @DataModel
    private List<SessionUserLog> sessionUserLogList;

    private String criteria;
    private List<String> sortPriority;


    @Create
    public void init() {
        sortPriority = new ArrayList<String>();
        sortPriority.add("online");
        sortPriority.add("lastAction");
    }

    @Factory("sessionUserLogList")
    public void loadSessionUserLogList() {
        sessionUserLogList = new ArrayList<SessionUserLog>(userSessionAction.getSessionUserLogMap().values());
    }

    /**
     * Search a log using a criteria over the user name.
     * The wildcard "%" is allowed in the criteria.
     */
    public void search() {
        loadSessionUserLogList();

        if (criteria != null && !criteria.equals("")) {
            List<SessionUserLog> filteredList = new ArrayList<SessionUserLog>();
            for (SessionUserLog sessionUserLog : sessionUserLogList) {

                String regex = "[\\w|\\W]*" + criteria.toLowerCase().replace("%", "[\\w|\\W]*") + "[\\w|\\W]*";
                if (sessionUserLog.getName().toLowerCase().matches(regex)) {
                    filteredList.add(sessionUserLog);
                }
            }
            sessionUserLogList = filteredList;
        }
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public List<String> getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(List<String> sortPriority) {
        this.sortPriority = sortPriority;
    }
}
