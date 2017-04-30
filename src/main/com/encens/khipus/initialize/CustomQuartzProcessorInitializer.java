package com.encens.khipus.initialize;

import com.encens.khipus.initialize.timer.CustomQuartzProcessorJob;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.core.ResourceLoader;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import java.beans.XMLDecoder;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * @author
 * @version 3.0
 */
@Name("customQuartzProcessorInitializer")
@Scope(ScopeType.APPLICATION)
@Install
@Startup(depends = "quartzDispatcher")
public class CustomQuartzProcessorInitializer {
    private static final LogProvider log = Logging.getLogProvider(CustomQuartzProcessorInitializer.class);
    private CustomQuartzProcessorSetting customQuartzProcessorSetting;

    private String xmlFilePath;

    @In
    CustomQuartzProcessorJob customQuartzProcessorJob;

    public void setXmlFilePath(String xmlFilePath) {
        if (null == xmlFilePath || "".equals(xmlFilePath.trim())) {
            throw new RuntimeException("xmlFilePath can't be null or empty, " +
                    "it should contain the xml configuration file path ie: '/WEB-INF/customquartzprocessors.xml'");
        }

        this.xmlFilePath = xmlFilePath;

        InputStream validationInputStream = ResourceLoader.instance().getResourceAsStream(this.xmlFilePath);
        XMLDecoder xmlDecoder = new XMLDecoder(validationInputStream);
        customQuartzProcessorSetting = (CustomQuartzProcessorSetting) xmlDecoder.readObject();
        xmlDecoder.close();
        CustomQuartzProcessorSetting.i = customQuartzProcessorSetting;
        //todo validate required objects params
    }

    /**
     * Starts the quartz scheduler
     */
    @Observer("org.jboss.seam.postInitialization")
    @SuppressWarnings({"UnusedDeclaration"})
    public void initIntegrationScheduler() {
        log.info("Initializing the CustomQuartzProcessorInitializer Quartz Scheduler Job");
        try {
            for (Map.Entry<String, CustomQuartzProcessor> stringCustomQuartzProcessorEntry : CustomQuartzProcessorSetting.i.getCustomQuartzProcessorMap().entrySet()) {
                CustomQuartzProcessor customQuartzProcessor = stringCustomQuartzProcessorEntry.getValue();
                if (customQuartzProcessor.getCustomQuartzProcessorType().equals(CustomQuartzProcessorType.INTERVAL_CRON)) {
                    QuartzTriggerHandle handle = customQuartzProcessorJob.performTask(new Date(), customQuartzProcessor.getIntervalCron(), stringCustomQuartzProcessorEntry.getKey());
                } else {
                    customQuartzProcessorJob.performTask(new Date(), customQuartzProcessor.getInterval(), stringCustomQuartzProcessorEntry.getKey());
                }
            }
        } catch (Exception e) {
            log.error("There was an error in the initialization of the integration scheduler", e);
        }
    }

}
