package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.production.ProductCompositionException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherType;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.encens.khipus.model.production.ProductionPlanningState.EXECUTED;


@Stateless
@Name("productionPlanningService")
@AutoCreate
public class ProductionPlanningServiceBean extends ExtendedGenericServiceBean implements ProductionPlanningService {

    @In
    private EvaluatorMathematicalExpressionsService evaluatorMathematicalExpressionsService;

    @Override
    public void refresh(Object entity) {
        getEntityManager().refresh(entity);
    }

    @Override
    public ProductionPlanning find(long id) {
        String query = "ProductionPlanning.widthProductionOrderAndProductCompositionAndProcessedProductFind";
        ProductionPlanning pp = (ProductionPlanning) getEntityManager().createNamedQuery(query)
                .setParameter("id", id)
                .getSingleResult();
        return pp;
    }

    @Override
    protected Object preUpdate(Object entity) {
        try {
            ProductionPlanning planning = (ProductionPlanning) entity;
            executeMathematicalFormulas(planning);
            //todo:aumentar la creacion automatica del vale para insumos de producccion
            //createVouchersForProductionInputs(planning);
            //createVouchersForProductionOutputs(planning);
            return entity;
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
    }
    //todo: buscar a la leche cruda y usar su id
    public Double calculateTotalMilk(ProductionPlanning planning){
     BigDecimal total_order = BigDecimal.ZERO;
     BigDecimal total_repro = BigDecimal.ZERO;
        try {
            total_order = (BigDecimal)getEntityManager().createNativeQuery("select nvl(sum(oi.cantidad),0) from ordenproduccion op\n" +
                    "inner join ordeninsumo oi\n" +
                    "on oi.idordenproduccion = op.idordenproduccion\n" +
                    "where oi.cod_art = '26'\n" +
                    "and oi.no_cia = '01'\n" +
                    "and op.idplanificacionproduccion = :planning")
                    .setParameter("planning",planning)
                    .getSingleResult();
        }catch (NoResultException e){
            total_order = BigDecimal.ZERO;
        }

        try {
            total_repro = (BigDecimal)getEntityManager().createNativeQuery("select nvl(sum(oi.cantidad),0) from productobase pb\n" +
                    "inner join ordeninsumo oi\n" +
                    "on oi.idproductobase = pb.idproductobase\n" +
                    "where oi.cod_art = '26'\n" +
                    "and oi.no_cia = '01'\n" +
                    "and pb.idplanificacionproduccion= :planning")
                    .setParameter("planning",planning)
                    .getSingleResult();
        }catch (NoResultException e){
            total_repro = BigDecimal.ZERO;
        }

        return total_repro.doubleValue() + total_order.doubleValue();
    }

    private void executeMathematicalFormulas(ProductionPlanning planning) throws IOException, ProductCompositionException {
        for (ProductionOrder po : planning.getProductionOrderList()) {
            if(po.getProductMain() == null)
            evaluatorMathematicalExpressionsService.executeMathematicalFormulas(po);
        }
    }

    private void createVouchersForProductionInputs(ProductionPlanning planning) throws IOException, ProductCompositionException {
        if (planning.getState() != EXECUTED)
            return;

        for (ProductionOrder po : planning.getProductionOrderList()) {
            if (po.getInputProductionVoucherList().size() > 0)
                continue;

            for (ProductionIngredient pi : po.getProductComposition().getProductionIngredientList()) {
                InputProductionVoucher voucher = new InputProductionVoucher();
                voucher.setProductionOrder(po);
                voucher.setMetaProduct(pi.getMetaProduct());
                voucher.setAmount(pi.getAmount());
                po.getInputProductionVoucherList().add(voucher);
            }
        }
    }

    private void createVouchersForProductionOutputs(ProductionPlanning planning) {
        if (planning.getState() != EXECUTED)
            return;

        for (ProductionOrder po : planning.getProductionOrderList()) {
            if (po.getOutputProductionVoucherList().size() > 0)
                continue;

            OutputProductionVoucher voucher = new OutputProductionVoucher();
            voucher.setProductionOrder(po);
            voucher.setProducedAmount(po.getExpendAmount());
            voucher.setProcessedProduct(po.getProductComposition().getProcessedProduct());
            po.getOutputProductionVoucherList().add(voucher);
        }
    }

    public BigDecimal getMountInWarehouse(MetaProduct metaProduct) {
        try{
        return (BigDecimal) getEntityManager()
                .createQuery("SELECT sum(inventory.unitaryBalance) from Inventory inventory where inventory.productItem = :productItem")
                .setParameter("productItem", metaProduct.getProductItem())
                .getSingleResult();
        }catch (NoResultException e)
        {
            return new BigDecimal(0.0);
        }
    }

    public BigDecimal getMountInWarehouse(ProductItem productItem) {
        BigDecimal result = BigDecimal.ZERO;
        try{
            result = (BigDecimal) getEntityManager()
                .createQuery("SELECT sum(inventory.unitaryBalance) from Inventory inventory where inventory.productItem = :productItem")
                .setParameter("productItem", productItem)
                .getSingleResult();

        }catch(NoResultException e)
        {
            return new BigDecimal(0.0);
        }
        if(result == null)
            return BigDecimal.ZERO;

        return result;
    }

    public BigDecimal getMountInWarehouse(Long id) {
        return (BigDecimal) getEntityManager()
                .createQuery("SELECT sum(inventory.unitaryBalance) from Inventory inventory where inventory.productItem.id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public WarehouseDocumentType getDefaultDocumentType() {
        List<WarehouseDocumentType> warehouseDocumentTypeList = getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findByType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("warehouseVoucherType", WarehouseVoucherType.C).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(warehouseDocumentTypeList)) {
            return warehouseDocumentTypeList.get(0);
        }

        return null;
    }

    @Override
    public WarehouseDocumentType getRecepcionDocumentType() {
        List<WarehouseDocumentType> warehouseDocumentTypeList = getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findByType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("warehouseVoucherType", WarehouseVoucherType.R).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(warehouseDocumentTypeList)) {
            return warehouseDocumentTypeList.get(0);
        }

        return null;
    }

    @Override
    public void updateProductionPlanning(ProductionPlanning instance,ProductionOrder order) throws ConcurrencyException, EntryDuplicatedException {
        try {

            for(IndirectCosts costs:order.getIndirectCostses()){
                if(costs.getId() != null)
                {
                    getEntityManager().remove(costs);
                    //order.getIndirectCostses().remove(costs);
                }
            }
            getEntityManager().merge(instance);
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) {
            throw new EntryDuplicatedException(ee);
        }
    }

    @Override
    public void updateProductionPlanning(ProductionPlanning instance) throws ConcurrencyException, EntryDuplicatedException {
        //To change body of implemented methods use File | Settings | File Templates.
        getEntityManager().merge(instance);
        getEntityManager().flush();
    }

    @Override
    public void deleteIndirectCost(ProductionOrder order)
    {
        getEntityManager().createNativeQuery("delete from costosindirectos where IDORDENPRODUCCION = :order")
                                            .setParameter("order",order)
                                            .executeUpdate();
    }

    @Override
    public void deleteIndirectCost(SingleProduct singleProduct)
    {
        getEntityManager().createNativeQuery("delete from costosindirectos where IDPRODUCTOSIMPLE = :id")
                .setParameter("id",singleProduct.getId())
                .executeUpdate();
        //getEntityManager().remove(singleProduct.getIndirectCostses());
    }

    @Override
    public List<IndirectCosts> findIndirectCostFromSingle(SingleProduct singleProduct) {
        List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();

        try{
            indirectCostses = getEntityManager().createQuery("select indirectCosts from IndirectCosts indirectCosts where indirectCosts.singleProduct = :singleProduct")
                              .setParameter("singleProduct",singleProduct)
                              .getResultList();

        }catch( NoResultException e )
        {
            return new ArrayList<IndirectCosts>();
        }

        return indirectCostses;
    }

    @Override
    public void updateOrder(ProductionOrder order) {
        getEntityManager().createNativeQuery("update ORDENPRODUCCION set PORCENTAJEGRASA = :percentage where IDORDENPRODUCCION = :order")
                          .setParameter("percentage",order.getGreasePercentage())
                          .setParameter("order",order)
                          .executeUpdate();
    }

    @Override
    public void addIndirectCostToSingleProduct(IndirectCosts indirectCosts) {
        getEntityManager().persist(indirectCosts);
    }

    public void updateProductionBase(BaseProduct base)
    {
        getEntityManager().merge(base);
        getEntityManager().flush();
    }

    public List<ProductionPlanning> getAllProductionPlanningByDates(Date startDate,Date endDate){
        List<ProductionPlanning> productionPlannings = new ArrayList<ProductionPlanning>();
        productionPlannings = (List<ProductionPlanning>)getEntityManager().createQuery("select productionPlanning from ProductionPlanning productionPlanning " +
                                                                                       " where productionPlanning.date between :startDate and :endDate")
                                                                          .setParameter("startDate",startDate, TemporalType.DATE)
                                                                          .setParameter("endDate",endDate,TemporalType.DATE)
                                                                          .getResultList();

        return productionPlannings;
    }

    @Override
    public void updateProductionPlanningDirect(ProductionPlanning instance) {
        getEntityManager().createNativeQuery("update planificacionproduccion set estado = :state \n" +
                "where idplanificacionproduccion =  :id ")
                .setParameter("state", instance.getState().toString())
                .setParameter("id",instance.getId())
                .executeUpdate();
    }

    @Override
    public List<OrderInput> getInputsAdd(ProductionOrder productionOrder) {
        List<OrderInput> orderInputs = new ArrayList<OrderInput>();
        if(productionOrder.getId() != null) {
            try {
                orderInputs = (List<OrderInput>) getEntityManager().createQuery("select orderInput from OrderInput orderInput " +
                        " where orderInput.productionOrder = :productionOrder " +
                        " and orderInput.type = 'ADD'")
                        .setParameter("productionOrder", productionOrder)
                        .getResultList();
            } catch (NoResultException e) {
                return orderInputs;
            }
        }
        return orderInputs;
    }

    @Override
    public void deleteOrderInput(OrderInput orderInput) {
        if(orderInput.getId() != null)
        getEntityManager().createQuery("delete from OrderInput orderInput where orderInput = :orderInput ")
                          .setParameter("orderInput", orderInput)
                          .executeUpdate();
    }

    @Override
    public BigDecimal getTotalMilkBySubGroup(String codGroup, String codSubGroup, Date startDate, Date endDate) {
        Double total = 0.0;
        List<ProductionPlanning> productionPlannings;
        try {
            productionPlannings = (List<ProductionPlanning>)getEntityManager()
                                                    .createQuery("select productionPlanning from ProductionPlanning productionPlanning " +
                                                                 " where productionPlanning.date between :startDate and :endDate ")
                                                    .setParameter("startDate",startDate,TemporalType.DATE)
                                                    .setParameter("endDate",endDate,TemporalType.DATE)
                                                    .getResultList();
        }catch(NoResultException e){
            return BigDecimal.ZERO;
        }
        for(ProductionPlanning productionPlanning:productionPlannings) {

            for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
                if (order.getProductComposition().getProcessedProduct().getProductItem().getGroupCode().compareTo(codGroup) == 0)
                    if (order.getProductComposition().getProcessedProduct().getProductItem().getSubGroupCode().compareTo(codSubGroup) == 0)
                        for (OrderInput input : order.getOrderInputs()) {
                            if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                                total += input.getAmount();
                        }

            }

            for (BaseProduct base : productionPlanning.getBaseProducts()) {
                Boolean band = false;
                double totalMilkBase = 0.0;

                for (OrderInput input : base.getOrderInputs()) {
                    if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0) {
                        totalMilkBase += input.getAmount();

                    }
                }

                for (SingleProduct single : base.getSingleProducts()) {
                    if (single.getProductProcessingSingle().getMetaProduct().getProductItem().getGroupCode().compareTo(codGroup) == 0)
                        if (single.getProductProcessingSingle().getMetaProduct().getProductItem().getSubGroupCode().compareTo(codSubGroup) == 0)
                            band = true;
                }

                if (band)
                    total+=totalMilkBase;

            }
        }
        return new BigDecimal(total);
    }

    public Double getTotalMilkByDateAndCodArt(Date date, String codArt) {
        Double total = 0.0;
        List<ProductionPlanning> productionPlannings;
        try {
            productionPlannings = (List<ProductionPlanning>)getEntityManager()
                    .createQuery("select productionPlanning from ProductionPlanning productionPlanning " +
                            " where productionPlanning.date = :date ")
                    .setParameter("date",date,TemporalType.DATE)
                    .getResultList();
        }catch(NoResultException e){
            return 0.0;
        }
        for(ProductionPlanning productionPlanning:productionPlannings) {

            for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
                if(order.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode().compareTo(codArt)==0)
                for (OrderInput input : order.getOrderInputs()) {
                    if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                        total += input.getAmount();
                }

            }

            /*for (BaseProduct base : productionPlanning.getBaseProducts()) {
                Double totalMilkBase = 0.0;
                Boolean band = false;

                for(SingleProduct single:base.getSingleProducts())
                {
                    if(single.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode().compareTo(codArt)==0)
                        band = true;
                }

                if(band)
                for (OrderInput input : base.getOrderInputs()) {
                    if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0) {
                        total += input.getAmount();

                    }
                }

            }*/
        }
        return total;
    }

    @Override
    public BigDecimal getTotalMilkByDate(Date startDate, Date endDate) {
        Double total = 0.0;
        List<ProductionPlanning> productionPlannings;
        try {
            productionPlannings = (List<ProductionPlanning>)getEntityManager()
                    .createQuery("select productionPlanning from ProductionPlanning productionPlanning " +
                            " where productionPlanning.date between :startDate and :endDate ")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .getResultList();
        }catch(NoResultException e){
            return BigDecimal.ZERO;
        }
        for(ProductionPlanning productionPlanning:productionPlannings) {

            for (ProductionOrder order : productionPlanning.getProductionOrderList()) {
                        for (OrderInput input : order.getOrderInputs()) {
                            if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0)
                                total += input.getAmount();
                        }

            }

            for (BaseProduct base : productionPlanning.getBaseProducts()) {
                for (OrderInput input : base.getOrderInputs()) {
                    if (input.getProductItemCode().compareTo(Constants.ID_ART_RAW_MILK) == 0) {
                        total += input.getAmount();

                    }
                }

            }
        }
        return new BigDecimal(total);
    }

    @Override
    public Double getTotalProducedOrderByArticleAndDate(String codArt, Date startDate, Date endDate) {
        Double total = 0.0;
        try {
            //For Oracle
            /*total = (Double)getEntityManager()
                    .createQuery("select nvl(sum(productionOrder.producedAmount),0.0) from ProductionPlanning productionPlanning " +
                            " inner join productionPlanning.productionOrderList productionOrder " +
                            " where productionPlanning.date between :startDate and :endDate " +
                            " and productionOrder.productComposition.processedProduct.productItem.productItemCode =:codArt")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("codArt",codArt)
                    .getSingleResult();*/

            Object obj = getEntityManager().createQuery("select sum(productionOrder.producedAmount) from ProductionPlanning productionPlanning " +
                    " inner join productionPlanning.productionOrderList productionOrder " +
                    " where productionPlanning.date between :startDate and :endDate " +
                    " and productionOrder.productComposition.processedProduct.productItem.productItemCode =:codArt")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("codArt",codArt)
                    .getSingleResult();

            if(obj == null) total = 0.0; else total = (Double)obj;

        }catch(NoResultException e){
            return 0.0;
        }
        return total;
    }

    @Override
    public Double getTotalProducedReproByArticleAndDate(String codArt, Date startDate, Date endDate) {
        Long total;

        try {
            //For Oracle
            /*total = (Long)getEntityManager()
                    .createQuery("select nvl(sum(singleProduct.amount),0) from ProductionPlanning productionPlanning " +
                            " inner join productionPlanning.baseProducts baseProduct " +
                            " inner join baseProduct.singleProducts singleProduct " +
                            " where productionPlanning.date between :startDate and :endDate " +
                            " and singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode =:codArt")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("codArt",codArt)
                    .getSingleResult();*/

            Object obj = getEntityManager().createQuery("select sum(singleProduct.amount) from ProductionPlanning productionPlanning " +
                            " inner join productionPlanning.baseProducts baseProduct " +
                            " inner join baseProduct.singleProducts singleProduct " +
                            " where productionPlanning.date between :startDate and :endDate " +
                            " and singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode =:codArt")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("codArt",codArt)
                    .getSingleResult();

            if(obj == null) total = 0L ; else total = (Long)obj;

        }catch(NoResultException e){
            return 0.0;
        }
        return total.doubleValue();
    }

    @Override
    public Double getProductionOrderSNGbyDateAndCodArt(String codArt, Date date) {
        Double total = 0.0;
        try {
            total = (Double)getEntityManager()
                    .createQuery("select productionOrder.greasePercentage from ProductionPlanning productionPlanning " +
                            " inner join productionPlanning.productionOrderList productionOrder " +
                            " where productionPlanning.date = :date " +
                            " and productionOrder.productComposition.processedProduct.productItem.productItemCode =:codArt")
                    .setParameter("date", date, TemporalType.DATE)
                    .setParameter("codArt", codArt)
                    .getSingleResult();
        }catch(NoResultException e){
            return 0.0;
        }
        return total;
    }

    @Override
    public Double getReproSNGbyDateAndCodArt(String codArt, Date date) {
        BigDecimal total;
        try {
            total = (BigDecimal)getEntityManager()
                    .createQuery("select singleProduct.greasePorentage from ProductionPlanning productionPlanning " +
                            " inner join productionPlanning.baseProducts baseProduct " +
                            " inner join baseProduct.singleProducts singleProduct " +
                            " where productionPlanning.date = :date " +
                            " and singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode =:codArt")
                    .setParameter("date", date, TemporalType.DATE)
                    .setParameter("codArt",codArt)
                    .getSingleResult();
        }catch(NoResultException e){
            return 0.0;
        }
        return total.doubleValue();
    }

    public void updateOrdenProduction(ProductionOrder order)
    {
        getEntityManager().createNativeQuery("update ordenproduccion set estadoorden = :state, id_tmpenc = :id_tmpenc, no_trans = :numTranst\n" +
                "where idordenproduccion = :id ")
                                      .setParameter("state", order.getEstateOrder().toString())
                                      .setParameter("numTranst",order.getNumberTransaction())
                                      .setParameter("id",order.getId())
                                      .setParameter("id_tmpenc", order.getVoucherId())
                                      .executeUpdate();

    }

    public void updateSingleProduct(SingleProduct singleProduct)
    {
        getEntityManager().createNativeQuery("update productosimple set estado = :state ,no_trans= :numTranst \n" +
                "where idproductosimple = :id")
                .setParameter("state",singleProduct.getState().toString())
                .setParameter("numTranst",singleProduct.getNumberTransaction())
                .setParameter("id",singleProduct.getId())
                .executeUpdate();
    }

    public void createProductionPlanningSingle(Date date)throws EntryDuplicatedException, ConcurrencyException {
        ProductionPlanning productionPlanning = new ProductionPlanning();
        productionPlanning.setDate(date);
        super.create(productionPlanning);
    }

}