package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetPart;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 3.1
 */

@Local
public interface FixedAssetBaseService extends GenericService {

    void updateFixedAssetSerialNumber(Long fixedAssetId, String serialNumber);

    void updateFixedAssetPartSerialNumber(Long fixedAssetPartId, String serialNumber);

    FixedAsset getFixedAsset(Long id);

    List<FixedAssetPart> getFixedAssetParts(Long fixedAssetId);


}
