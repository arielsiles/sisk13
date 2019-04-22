package com.encens.khipus.service.production;

import com.encens.khipus.model.production.Production;

import javax.ejb.Local;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface ProductionService {

    void createProduction(Production production);

}
