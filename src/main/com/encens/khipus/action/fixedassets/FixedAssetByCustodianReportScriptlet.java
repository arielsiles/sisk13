package com.encens.khipus.action.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAssetMovement;
import com.encens.khipus.service.fixedassets.FixedAssetMovementService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

/**
 * Encens S.R.L.
 * This class implements the scriptlet for the fixed assets by custodian report action, this scriptlet load data from the
 * KHIPUS database using some services
 *
 * @author
 * @version 2.2
 */
public class FixedAssetByCustodianReportScriptlet extends JRDefaultScriptlet {
    private FixedAssetMovementService fixedAssetMovementService = (FixedAssetMovementService) Component.getInstance("fixedAssetMovementService");

    /**
     * Finds the last approved fixedAssetMovement for the row (return it in a variable value)
     *
     * @throws JRScriptletException Exception
     */
    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Long fixedAssetId = (Long) this.getFieldValue("fixedAsset.id");
        FixedAssetMovement fixedAssetMovement = fixedAssetMovementService.findLastApprovedFixedAssetMovement(fixedAssetId);
        this.setVariableValue("fixedAssetLastMovement", fixedAssetMovement);
    }
}
