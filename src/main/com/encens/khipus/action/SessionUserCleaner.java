package com.encens.khipus.action;

import com.encens.khipus.util.KhipusCacheManager;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

/**
 * Encens S.R.L.
 * Util to execute process before of session destroy
 *
 * @author
 * @version $Id: SessionUserCleaner.java  20-may-2010 18:57:08$
 */
@Name("sessionUserCleaner")
public class SessionUserCleaner {
    @Logger
    private Log log;

    /**
     * pre destroy session event
     */
//    @Observer("org.jboss.seam.preDestroyContext.SESSION")
    public void preDestroyUserSessionContext() {

    }

    private void deleteTemporalSessionFolder() {
        log.debug("Executing deleteTemporalSessionFolder...");
        String sessionId = JSFUtil.getHttpSession(false).getId();
        KhipusCacheManager.deleteSessionFolder(sessionId);
    }
}
