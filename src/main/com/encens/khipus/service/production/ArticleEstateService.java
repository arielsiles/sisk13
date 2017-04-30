package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ArticleEstate;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface ArticleEstateService extends GenericService {

    public Boolean verifyEstate(ProductItem productItem, String compare );

    public Boolean existArticleEstate(ProductItem articleEstate);

}
