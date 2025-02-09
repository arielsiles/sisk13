package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.xproduction.ProductionLine;

import javax.ejb.Local;

/**
 * ProductionLineService service interface
 *
 * @author
 * @version 2.7
 */

@Local
public interface ProductionLineService extends GenericService {

    ProductionLine getDefaultProductionLine();

}