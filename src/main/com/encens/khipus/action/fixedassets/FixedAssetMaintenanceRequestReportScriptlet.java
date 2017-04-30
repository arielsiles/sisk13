package com.encens.khipus.action.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequest;
import com.encens.khipus.service.fixedassets.FixedAssetMaintenanceService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.util.Iterator;
import java.util.List;

/**
 * @author
 */
public class FixedAssetMaintenanceRequestReportScriptlet extends JRDefaultScriptlet{

    FixedAssetMaintenanceService fixedAssetMaintenanceService = (FixedAssetMaintenanceService) Component.getInstance("fixedAssetMaintenanceService");

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Object requestId=this.getFieldValue("fixedAssetMaintenanceRequest.id");
        if(requestId!=null){
            Long maintenanceRequestId= (Long) requestId;
            FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest=fixedAssetMaintenanceService.findFixedAssetMaintenanceRequestWithFixedAssets(maintenanceRequestId);
            if(fixedAssetMaintenanceRequest!=null){
                String fixedAssetBarCodes="";
                List<FixedAsset> fixedAssetList=fixedAssetMaintenanceRequest.getFixedAssets();
                for (Iterator<FixedAsset> iterator = fixedAssetList.iterator(); iterator.hasNext();) {
                    FixedAsset next = iterator.next();
                    fixedAssetBarCodes+=next.getBarCode();
                    if(iterator.hasNext()){
                        fixedAssetBarCodes+=", ";
                    }
                }
                this.setVariableValue("fixedAssetBarCodes", fixedAssetBarCodes);
            }
            else{
                this.setVariableValue("fixedAssetBarCodes", "");
            }
        }
    }
}
