package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.fixedassets.Model;
import com.encens.khipus.model.fixedassets.Trademark;

import javax.ejb.Local;

/**
 * @author
 * @version 2.25
 */

@Local
public interface TrademarkSynchronizeService extends GenericService {
    Trademark synchronizeTrademark(Trademark trademark, String trademarkName);

    Model synchronizeModel(Model model, String modelName);
}
