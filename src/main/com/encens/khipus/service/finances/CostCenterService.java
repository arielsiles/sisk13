package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CostCenter;

import javax.ejb.Local;

/**
 * Encens S.R.L.
 * This class implements the costCenter service local interface
 *
 * @author
 * @version 2.0.2
 */
@Local
public interface CostCenterService {
    CostCenter findCostCenterByCode(String code);
}
