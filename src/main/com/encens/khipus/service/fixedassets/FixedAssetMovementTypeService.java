package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.FixedAssetMovementType;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the FixedAssetMovementType service local interface
 *
 * @author
 * @version 2.25
 */
@Local
public interface FixedAssetMovementTypeService extends GenericService {
    @SuppressWarnings(value = "unchecked")
    List<FixedAssetMovementType> findAll();
}
