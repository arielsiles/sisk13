package com.encens.khipus.service.xproduction;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.xproduction.BaseProduct;
import com.encens.khipus.model.xproduction.ProductionOrder;
import com.encens.khipus.model.xproduction.ProductionProduct;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 2/11/13
 * Time: 18:45
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface ProductionOrderService{

    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException;
    public List<ProductionOrder> findProductionOrdersByProductItem(String productItemCode, Date startDate, Date endDate);

    List<ProductionProduct> findProductionByProductItem(String productItemCode, Date startDate, Date endDate);
    List<ProductionProduct> findProductionByDate(Date startDate, Date endDate);

    public List<ProductionOrder> findProductionOrders(Date startDate, Date endDate);
    public List<BaseProduct> findBaseProductByDate(Date startDate, Date endDate);

}
