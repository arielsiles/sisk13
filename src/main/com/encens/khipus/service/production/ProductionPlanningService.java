package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Local
public interface ProductionPlanningService extends GenericService {

    public void refresh(Object entity);

    public ProductionPlanning find(long id);

    public BigDecimal getMountInWarehouse(MetaProduct metaProduct);

    public BigDecimal getMountInWarehouse(Long id);

    public BigDecimal getMountInWarehouse(ProductItem productItem);

    public WarehouseDocumentType getDefaultDocumentType();

    public WarehouseDocumentType getRecepcionDocumentType();

    public void updateProductionPlanning(ProductionPlanning instance,ProductionOrder order) throws ConcurrencyException, EntryDuplicatedException;

    public void updateProductionPlanning(ProductionPlanning instance) throws ConcurrencyException, EntryDuplicatedException;

    public void deleteIndirectCost(ProductionOrder order);

    public void deleteIndirectCost(SingleProduct singleProduct);

    public List<IndirectCosts> findIndirectCostFromSingle(SingleProduct singleProduct);

    public void updateOrder(ProductionOrder order);

    public void addIndirectCostToSingleProduct(IndirectCosts indirectCosts);

    public Double calculateTotalMilk(ProductionPlanning planning);

    public void updateOrdenProduction(ProductionOrder order);

    public void updateProductionBase(BaseProduct base);

    public void updateSingleProduct(SingleProduct singleProduct);

    public List<ProductionPlanning> getAllProductionPlanningByDates(Date startDate,Date endDate);

    public void updateProductionPlanningDirect(ProductionPlanning instance);

    List<OrderInput> getInputsAdd(ProductionOrder productionOrder);

    void deleteOrderInput(OrderInput orderInput);

    BigDecimal getTotalMilkBySubGroup(String codGroup, String codSubGroup, Date startDate, Date endDate);

    Double getTotalMilkByDateAndCodArt(Date date, String codArt);

    BigDecimal getTotalMilkByDate(Date startDate, Date endDate);

    Double getTotalProducedOrderByArticleAndDate(String codArt, Date startDate, Date endDate);

    Double getTotalProducedReproByArticleAndDate(String codArt, Date startDate, Date endDate);

    Double getProductionOrderSNGbyDateAndCodArt(String codArt, Date date);

    Double getReproSNGbyDateAndCodArt(String codArt, Date date);
}
