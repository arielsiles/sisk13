package com.encens.khipus.dataintegration.timer;

import com.encens.khipus.dataintegration.configuration.Configuration;
import com.encens.khipus.dataintegration.configuration.structure.IntegrationElement;
import com.encens.khipus.dataintegration.service.DataIntegrationService;
import com.encens.khipus.initialize.timer.CustomQuartzProcessorSync;
import com.encens.khipus.util.ExecutionTimeUtil;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import java.util.List;

/**
 * The component that handles the Quartz schedule, performing invocation to the data integration services defined
 * in the configuration.
 *
 * @author
 * @version 3.2.10
 */

@Name("dataIntegrationQuartzJob")
@AutoCreate
public class DataIntegrationQuartzJob {
    @Logger
    private Log log;
    private static final String SERVICE_SEAM_NAME = "dataIntegrationService";

    /**
     * This method performs the logic every time the quartz scheduler timeout
     *
     * @param when     the date when the scheduler must start
     * @param interval the interval time(milliseconds) between each invocation. This jobs performs without time limit.
     * @return the Quartz handler in the case you'd need to stop, pause or resume the job. Not needed in this case.
     */
    @Asynchronous
    @Transactional(TransactionPropagationType.NEVER)
    @SuppressWarnings({"UnusedDeclaration"})
    public QuartzTriggerHandle performTask(@Expiration java.util.Date when, @IntervalDuration long interval) {
        if (CustomQuartzProcessorSync.i.begin(SERVICE_SEAM_NAME)) {
            try {

                List<IntegrationElement> integrationElements = Configuration.i.getIntegrationElementList();
                ExecutionTimeUtil executionTimeUtil = new ExecutionTimeUtil();
                executionTimeUtil.startExecution();
                for (IntegrationElement integrationElement : integrationElements) {
                    DataIntegrationService service = (DataIntegrationService)
                            Component.getInstance(integrationElement.getServiceSeamName());
                    service.executeIntegration(Configuration.i.getLocalDataSource(), integrationElement);
                }
                CustomQuartzProcessorSync.i.end(SERVICE_SEAM_NAME);
                executionTimeUtil.endExecution();
                log.info("\t\t\t\tEXECUTION TIME FOR WISE DATA INTEGRATION: ");
                log.info("\t\texecutionTimeUtil.timeInSecons(): " + executionTimeUtil.timeInSecons());
                log.info("\t\texecutionTimeUtil.timeInMillis(): " + executionTimeUtil.timeInMillis());
            } catch (Exception e) {
                CustomQuartzProcessorSync.i.end(SERVICE_SEAM_NAME);
            }
        }
        return null;
    }
}
