package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAssetMaintenanceRequest;

import javax.ejb.Local;

/**
 * @author
 * @version 2.25
 */
@Local
public interface FixedAssetMaintenanceService extends GenericService {
    FixedAssetMaintenanceRequest findFixedAssetMaintenanceRequestWithFixedAssets(Long maintenanceRequestId);
}
