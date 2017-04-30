package com.encens.khipus.dashboard.util;

import com.encens.khipus.dashboard.configuration.DashboardConfiguration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceLoader;

import java.io.InputStream;

/**
 * @author
 * @version 2.26
 */
@Name("dashboardConfigurationInitializer")
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class DashboardConfigurationInitializer {
    private String xmlFilePath;

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        if (null == xmlFilePath || "".equals(xmlFilePath.trim())) {
            throw new RuntimeException("xmlFilePath cannot be null or empty, " +
                    "it should be contain the xml configuration file path eg: '/WEB-INF/dashboard-def.xml'");
        }

        this.xmlFilePath = xmlFilePath;

        InputStream validationInputStream = ResourceLoader.instance().getResourceAsStream(this.xmlFilePath);
        DashboardConfiguration.INSTANCE.validate(validationInputStream);

        InputStream xmlInputStream = ResourceLoader.instance().getResourceAsStream(this.xmlFilePath);
        DashboardConfiguration.INSTANCE.initialize(xmlInputStream);
    }
}
