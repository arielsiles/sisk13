package com.encens.khipus.dataintegration.configuration;

import com.encens.khipus.dataintegration.configuration.structure.IntegrationElement;
import com.encens.khipus.dataintegration.configuration.structure.XmlConfiguration;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.List;

/**
 * Singleton class that encapsulate  <code>XmlConfiguration</code> object and <code>StructureBuilder</code> object.
 * <p/>
 * For the class works correctly it is necessary follow the next steps:
 * <p/>
 * 1.- Validate the <code>InputStream</code> object that contain the XML file.
 * eg:
 * <code>Configuration.i.validate(validationInputStream);</code>
 * <p/>
 * 2.- Initialize the <code>XmlConfiguration</code> object
 * eg:
 * <code>Configuration.i.initialize(xmlInputStream);</code>
 * <p/>
 * After of this the singleton class should be contain a reference of the <code>XmlConfiguration</code> object.
 *
 * @author
 */
public class Configuration {
    private XmlConfiguration xmlConfiguration = null;

    private StructureBuilder builder;

    public static Configuration i = new Configuration();

    private Configuration() {
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
        if (null == xmlConfiguration) {
            xmlConfiguration = builder.buildConfiguration(xmlInputStream);
        }
    }

    public List<IntegrationElement> getIntegrationElementList() {
        checkXmlConfiguration();
        return xmlConfiguration.getIntegrationElements();
    }

    public Long getTimerInterval() {
        checkXmlConfiguration();
        return xmlConfiguration.getTimerInterval();
    }

    public String getLocalDataSource() {
        checkXmlConfiguration();
        return xmlConfiguration.getLocalDataSource();
    }

    public boolean isInitialized() {
        return null != xmlConfiguration;
    }

    private void checkXmlConfiguration() {
        if (null == xmlConfiguration) {
            throw new RuntimeException("Configuration.initialize(java.io.InputStream) should be excecuted first.");
        }
    }
}
