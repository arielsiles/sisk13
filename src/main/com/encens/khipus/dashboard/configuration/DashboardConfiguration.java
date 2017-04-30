package com.encens.khipus.dashboard.configuration;

import com.encens.khipus.dashboard.configuration.structure.XmlDashboard;
import com.encens.khipus.dashboard.configuration.structure.XmlWidget;
import org.xml.sax.SAXException;

import java.io.InputStream;

/**
 * Singleton class that encapsulate <code>XmlDashboard</code> object and <code>StructureBuilder</code> object.
 * <p/>
 * For the class works correctly it is necessary follow the next steps:
 * <p/>
 * 1.- Validate the <code>InputStream</code> object that contain the XML file.
 * eg:
 * <code>DashboardConfiguration.INSTANCE.validate(validationInputStream);</code>
 * <p/>
 * 2.- Initialize the <code>XmlDashboard</code> object
 * eg:
 * <code>DashboardConfiguration.INSTANCE.initialize(xmlInputStream);</code>
 * <p/>
 * After of this the singleton class should be contain a reference of the <code>XmlDashboard</code> object.
 *
 * @author
 * @version 2.26
 */
public enum DashboardConfiguration {
    INSTANCE;

    private XmlDashboard xmlDashboard = null;
    private StructureBuilder builder;

    DashboardConfiguration() {
        builder = new StructureBuilder();
    }

    public void validate(InputStream xmlInputStream) {
        try {
            builder.validate(xmlInputStream);
        } catch (SAXException e) {
            throw new RuntimeException("Cannot validate xml file. ", e);
        }
    }

    public void initialize(InputStream xmlInputStream) {
        if (null == xmlDashboard) {
            xmlDashboard = builder.buildDashboard(xmlInputStream);
        }
    }

    public XmlDashboard getDashboard() {
        return xmlDashboard;
    }

    public XmlWidget getWidget(String id) {
        for (XmlWidget xmlWidget : xmlDashboard.getWidgets()) {
            if (xmlWidget.getId().equals(id)) {
                return xmlWidget;
            }
        }
        return null;
    }

    public boolean isInitialized() {
        return null != xmlDashboard;
    }
}
