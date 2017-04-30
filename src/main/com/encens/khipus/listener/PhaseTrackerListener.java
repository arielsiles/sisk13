package com.encens.khipus.listener;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * @author
 * @version 1.0
 */
public class PhaseTrackerListener implements PhaseListener {

    private static final LogProvider log = Logging.getLogProvider(PhaseTrackerListener.class);
    private long time;

    public void afterPhase(PhaseEvent phaseEvent) {
        long ctime = System.currentTimeMillis();


        log.info(phaseEvent.getPhaseId());
        log.info("Time taken in this phase: " + (ctime - time) / 1000);
    }

    public void beforePhase(PhaseEvent phaseEvent) {
        time = System.currentTimeMillis();

        log.info(phaseEvent.getPhaseId());

    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}
