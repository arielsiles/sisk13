package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.LogProductiveZone;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface LogProductiveZoneService {

    public void createLog(LogProductiveZone logProductiveZone);

}
