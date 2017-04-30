package com.encens.khipus.dataintegration.util;

import com.encens.khipus.dataintegration.configuration.Configuration;
import com.encens.khipus.dataintegration.timer.DataIntegrationQuartzJob;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import java.io.InputStream;

/**
 * Initialize the DataIntegration components, this is:
 * <p/>
 * - Initialize <code>com.encens.khipus.dataintegration.configuration.Configuration</code> singleton.
 * - Starts the <code>DataIntegrationQuartzJob</code> scheduler.
 *
 * @author
 */

@Name("dataIntegrationInitializer")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.APPLICATION)
@Startup(depends = "quartzDispatcher")
public class DataIntegrationInitializer {

    private static final LogProvider log = Logging.getLogProvider(DataIntegrationInitializer.class);
    private String xmlFilePath;

    @In
    DataIntegrationQuartzJob dataIntegrationQuartzJob;

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        if (null == xmlFilePath || "".equals(xmlFilePath.trim())) {
            throw new RuntimeException("xmlFilePath cannot be null or empty, " +
                    "it should be contain the xml configuration file path eg: '/WEB-INF/dataintegration.xml'");
        }

        this.xmlFilePath = xmlFilePath;


        InputStream validationInputStream = ResourceLoader.instance().getResourceAsStream(this.xmlFilePath);
        Configuration.i.validate(validationInputStream);

        InputStream xmlInputStream = ResourceLoader.instance().getResourceAsStream(this.xmlFilePath);
        Configuration.i.initialize(xmlInputStream);

    }

    /**
     * Starts the quartz scheduler
     */
    @Observer("org.jboss.seam.postInitialization")
    @SuppressWarnings({"UnusedDeclaration"})
    public void initIntegrationScheduler() {
        log.info("Initializing the Data Integration Quartz Scheduler Job");
        try {
            //dataIntegrationQuartzJob.performTask(new Date(), Configuration.i.getTimerInterval());
        } catch (Exception e) {
            log.error("There was an error in the initialization of the integration scheduler", e);
        }
    }

}
