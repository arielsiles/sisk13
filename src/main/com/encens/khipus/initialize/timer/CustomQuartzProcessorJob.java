package com.encens.khipus.initialize.timer;

import com.encens.khipus.initialize.service.CustomQuartzProcessorService;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;

import java.util.Date;

/**
 * @author
 * @version 3.0
 */
@Name("customQuartzProcessorJob")
@AutoCreate
public class CustomQuartzProcessorJob {
    /**
     * This method performs the logic every time the quartz scheduler timeout
     *
     * @param when            the date when the scheduler must start
     * @param interval        the interval time(milliseconds) between each invocation. This jobs performs without time limit.
     * @param serviceSeamName the seam service name
     * @return the Quartz handler in the case you'd need to stop, pause or resume the job. Not needed in this case.
     */
    @Asynchronous
    @Transactional(TransactionPropagationType.NEVER)
    @SuppressWarnings({"UnusedDeclaration"})
    public QuartzTriggerHandle performTask(@Expiration java.util.Date when, @IntervalDuration long interval, String serviceSeamName) {
        if (CustomQuartzProcessorSync.i.begin(serviceSeamName)) {
            try {
                CustomQuartzProcessorService service = (CustomQuartzProcessorService)
                        Component.getInstance(serviceSeamName);
                service.execute();
            } catch (Exception e) {
                CustomQuartzProcessorSync.i.end(serviceSeamName);
            }
        }
        return null;
    }

    /**
     * This method performs the logic at a specified time
     *
     * @param when            the date when the scheduler must start
     * @param cron            the frequence for each invocation. This jobs performs without time limit.
     * @param serviceSeamName the seam service name
     * @return the Quartz handler in the case you'd need to stop, pause or resume the job. Not needed in this case.
     */
    @Asynchronous
    @Transactional(TransactionPropagationType.NEVER)
    @SuppressWarnings({"UnusedDeclaration"})
    public QuartzTriggerHandle performTask(@Expiration Date when, @IntervalCron String cron, String serviceSeamName) {
        if (CustomQuartzProcessorSync.i.begin(serviceSeamName)) {
            try {
                CustomQuartzProcessorService service = (CustomQuartzProcessorService)
                        Component.getInstance(serviceSeamName);
                service.execute();
            } catch (Exception e) {
                CustomQuartzProcessorSync.i.end(serviceSeamName);
            }
        }
        return null;
    }
}
