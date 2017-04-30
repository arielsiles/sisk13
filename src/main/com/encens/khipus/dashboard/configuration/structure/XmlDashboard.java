package com.encens.khipus.dashboard.configuration.structure;

import java.util.List;

/**
 * @author
 * @version 2.26
 */
public class XmlDashboard {

    private List<XmlWidget> xmlWidgets;

    public List<XmlWidget> getWidgets() {
        return xmlWidgets;
    }

    public void setWidgets(List<XmlWidget> xmlWidgets) {
        this.xmlWidgets = xmlWidgets;
    }
}
