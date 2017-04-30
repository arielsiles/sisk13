package com.encens.khipus.action.dashboard;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DashboardPanelAction
 *
 * @author
 * @version 2.7
 */
@Name("dashboardPanelAction")
@Scope(ScopeType.PAGE)
public class DashboardPanelAction implements Serializable {

    private Map<String, Boolean> showComponent = new HashMap<String, Boolean>() {
        @Override
        public Boolean get(Object key) {
            Boolean value = super.get(key);
            if (value == null) {
                put((String) key, value = false);
            }
            return value;
        }
    };
    @In(required = false, scope = ScopeType.SESSION)
    @Out(required = false, scope = ScopeType.SESSION)
    private String selectedTab = "";

    public Map<String, Boolean> getShowComponent() {
        return showComponent;
    }

    public void setShowComponent(Map<String, Boolean> showComponent) {
        this.showComponent = showComponent;
    }

    public void enableShowComponent(String name) {
        getShowComponent().put(name, true);
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }
}
