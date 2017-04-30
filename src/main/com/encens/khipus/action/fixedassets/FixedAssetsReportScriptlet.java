package com.encens.khipus.action.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import com.encens.khipus.service.fixedassets.FixedAssetMovementService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.util.Date;

/**
 * Encens S.R.L.
 * Scriptlet to management fixed assets report
 *
 * @author
 * @version $Id: FixedAssetsReportScriptlet.java  21-oct-2010 17:04:54$
 */
public class FixedAssetsReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(FixedAssetsReportScriptlet.class);
    private FixedAssetMovementService fixedAssetMovementService = (FixedAssetMovementService) Component.getInstance("fixedAssetMovementService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Long fixedAssetId = getFieldAsLong("fixedAsset.id");

        setFixedAssetCancelCause(fixedAssetId);
        setLastFixedAssetMovementData(fixedAssetId);
    }

    private void setFixedAssetCancelCause(Long fixedAssetId) throws JRScriptletException {
        String cancelCause = "";
        FixedAssetMovement fixedAssetMovement = fixedAssetMovementService.findCancelFixedAssetMovement(fixedAssetId);

        if (fixedAssetMovement != null && fixedAssetMovement.getCause() != null) {
            cancelCause = fixedAssetMovement.getCause();
        }
        this.setVariableValue("cancelCauseVar", cancelCause);
    }

    private void setLastFixedAssetMovementData(Long fixedAssetId) throws JRScriptletException {
        log.debug("Executing setLastFixedAssetMovementData......");
        String lastMovement = "";
        Date movementDate = null;
        String responsibleMovement = "";

        FixedAssetMovement fixedAssetMovement = fixedAssetMovementService.findLastApprovedFixedAssetMovement(fixedAssetId);
        if (fixedAssetMovement != null) {
            lastMovement = (fixedAssetMovement.getFixedAssetMovementType() != null) ? fixedAssetMovement.getFixedAssetMovementType().getDescription() : "";
            movementDate = fixedAssetMovement.getMovementDate();
            responsibleMovement = fixedAssetMovement.getUserNumber();
        }

        this.setVariableValue("lastMovementVar", lastMovement);
        this.setVariableValue("movementDateVar", movementDate);
        this.setVariableValue("responsibleMovementVar", responsibleMovement);
    }


    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }
}
