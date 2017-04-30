package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequest;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * @author
 * @version 2.25
 */
@Stateless
@Name("fixedAssetMaintenanceService")
@AutoCreate
public class FixedAssetMaintenanceServiceBean extends GenericServiceBean implements FixedAssetMaintenanceService {

    public FixedAssetMaintenanceRequest findFixedAssetMaintenanceRequestWithFixedAssets(Long maintenanceRequestId) {
        return (FixedAssetMaintenanceRequest) getEntityManager().createNamedQuery("FixedAssetMaintenanceRequest.readWithFixedAsset")
                .setParameter("fixedAssetMaintenanceRequestId", maintenanceRequestId).getSingleResult();
    }
}
