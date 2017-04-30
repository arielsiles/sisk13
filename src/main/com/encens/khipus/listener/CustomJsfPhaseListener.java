package com.encens.khipus.listener;

import org.jboss.seam.jsf.SeamPhaseListener;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

/**
 * @author
 */
public class CustomJsfPhaseListener extends SeamPhaseListener {
    private static final LogProvider log = Logging.getLogProvider(CustomJsfPhaseListener.class);


    @Override
    protected void beforeRenderResponse(FacesContext facesContext) {
        log.debug("----> Start beforeRenderResponse(FacesContext facesContext) method.");
        super.beforeRenderResponse(facesContext);
        log.debug("----> End beforeRenderResponse(FacesContext facesContext) method.");
    }

    @Override
    protected void afterResponseComplete(FacesContext facesContext) {
        log.debug("----> Start afterResponseComplete(FacesContext facesContext) method.");
        super.afterResponseComplete(facesContext);
        log.debug("----> End afterResponseComplete(FacesContext facesContext) method.");
    }


    @Override
    protected void afterRenderResponse(FacesContext facesContext) {
        log.debug("---->  Start afterRenderResponse(FacesContext facesContext) method.");
        super.afterRenderResponse(facesContext);
        log.debug("---->  Dnd afterRenderResponse(FacesContext facesContext) method.");
    }


    @Override
    public void raiseEventsBeforePhase(PhaseEvent event) {
        log.debug("---->  Start raiseEventsBeforePhase(PhaseEvent event) method.");
        super.raiseEventsBeforePhase(event);
        log.debug("---->  End raiseEventsBeforePhase(PhaseEvent event) method.");
    }

    @Override
    public void raiseEventsAfterPhase(PhaseEvent event) {
        log.debug("----> Start raiseEventsAfterPhase(PhaseEvent event)  method.");
        super.raiseEventsAfterPhase(event);
        log.debug("----> End raiseEventsAfterPhase(PhaseEvent event)  method.");
    }

    @Override
    protected void raiseTransactionFailedEvent() {
        log.debug("----> Start raiseTransactionFailedEvent() method.");
        super.raiseTransactionFailedEvent();
        log.debug("----> End raiseTransactionFailedEvent() method.");
    }

    @Override
    protected void afterRestoreView(FacesContext facesContext) {
        log.debug("----> Start afterRestoreView(facesContext) method.");
        super.afterRestoreView(facesContext);
        log.debug("----> End afterRestoreView(facesContext) method.");
    }

    @Override
    protected void beforeRestoreView(FacesContext facesContext) {
        log.debug("----> Start beforeRestoreView(FacesContext facesContext) method.");
        super.beforeRestoreView(facesContext);
        log.debug("----> End beforeRestoreView(FacesContext facesContext) method.");
    }
}
