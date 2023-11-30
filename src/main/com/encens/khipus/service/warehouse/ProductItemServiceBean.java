package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.warehouse.ProductItemMinimalStockIsGreaterThanMaximumStockException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("productItemService")
@AutoCreate
public class ProductItemServiceBean extends GenericServiceBean implements ProductItemService {

    @In(value = "#{entityManager}")
    private EntityManager em;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private GenericService genericService;
    @In
    BusinessUnitService businessUnitService;

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void createProductItem(ProductItem productItem)
            throws EntryDuplicatedException, ProductItemMinimalStockIsGreaterThanMaximumStockException {
        try {
            validate(productItem);
            if (null == productItem.getGroupMeasureUnit()) {
                //noinspection NullableProblems
                productItem.setEquivalentQuantity(null);
            }
            productItem.setUnitCost(BigDecimal.ZERO);
            productItem.setCu(BigDecimal.ZERO);
            productItem.setInvestmentAmount(BigDecimal.ZERO);
            productItem.setCt(BigDecimal.ZERO);
            productItem.getId().setProductItemCode(String.valueOf(sequenceGeneratorService.nextValue(Constants.WAREHOUSE_PRODUCT_ITEM_SEQUENCE)));
            getEntityManager().persist(productItem);
            getEntityManager().flush();

            InitialInventory inv = new InitialInventory();
            inv.setProductItemCode(productItem.getId().getProductItemCode());
            inv.setQuantity(BigDecimal.ZERO);
            inv.setProductItemName(productItem.getName());
            inv.setUnitCost(BigDecimal.ZERO);
            inv.setCompanyNumber(productItem.getCompanyNumber());
            inv.setYear(DateUtils.getCurrentYear(new Date()).toString());
            inv.setWarehouseCode(productItem.getWarehouse().getWarehouseCode());

            getEntityManager().persist(inv);
            createProductInventory(productItem); //Crea inv_inventario
            getEntityManager().flush();

        } catch (PersistenceException e) {
            throw new EntryDuplicatedException();
        }
    }

    public void createProductInventory(ProductItem productItem){


        try {
            CompanyConfiguration companyConfiguration = genericService.findById(CompanyConfiguration.class, Constants.defaultCompanyNumber);
            BusinessUnit businessUnit = businessUnitService.findBusinessUnitByExecutorUnitCode(companyConfiguration.getCompanyNumber());

            Inventory inventory = new Inventory();
            //inventory.setId(new InventoryPK(productItem.getId().getCompanyNumber(), productItem.getWarehouseCode(), productItem.getId().getProductItemCode()));
            inventory.setId(new InventoryPK(productItem.getCompanyNumber(), productItem.getWarehouseCode(), productItem.getId().getProductItemCode()));
            inventory.setUnitaryBalance(BigDecimal.ZERO);

            InventoryDetail inventoryDetail = new InventoryDetail();
            inventoryDetail.setCostCenterCode(companyConfiguration.getExchangeRateBalanceCostCenter().getId().getCode());
            inventoryDetail.setCostCenter(companyConfiguration.getExchangeRateBalanceCostCenter());
            //inventoryDetail.setCompanyNumber(companyConfiguration.getCompanyNumber());
            inventoryDetail.setCompanyNumber(productItem.getCompanyNumber());
            inventoryDetail.setWarehouseCode(productItem.getWarehouseCode());
            inventoryDetail.setProductItem(productItem);
            inventoryDetail.setProductItemCode(productItem.getId().getProductItemCode());
            inventoryDetail.setQuantity(BigDecimal.ZERO);
            inventoryDetail.setExecutorUnit(businessUnit);

            getEntityManager().persist(inventory);
            getEntityManager().persist(inventoryDetail);

        }catch (Exception e) {
            log.error("Unexpected error ", e);
        }

    }

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void updateProductItem(ProductItem productItem)
            throws EntryDuplicatedException, ProductItemNotFoundException,
            ConcurrencyException, ProductItemMinimalStockIsGreaterThanMaximumStockException {
        validate(productItem);
        if (null == productItem.getGroupMeasureUnit()) {
            //noinspection NullableProblems
            productItem.setEquivalentQuantity(null);
        }
        super.update(productItem);
    }


    /**
     * Validates minimun and maximum stock values
     *
     * @param productItem the instance to validate
     * @throws ProductItemMinimalStockIsGreaterThanMaximumStockException
     *          thrown when minimun > maximum stock
     */
    private void validate(ProductItem productItem)
            throws ProductItemMinimalStockIsGreaterThanMaximumStockException {
        if (null != productItem.getMinimalStock() && null != productItem.getMaximumStock()
                && productItem.getMinimalStock().compareTo(productItem.getMaximumStock()) > 0) {
            throw new ProductItemMinimalStockIsGreaterThanMaximumStockException();
        }
    }

    /**
     * Finds a list of ProductItems involved in a WarehouseVoucher
     *
     * @param warehouseVoucher a given WarehouseVoucher
     * @return a list of ProductItems involved in a WarehouseVoucher
     */
    @SuppressWarnings(value = "unchecked")
    public List<ProductItem> findByWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        List<ProductItem> productItemList = new ArrayList<ProductItem>();
        List<ProductItem> resultList = getEntityManager().createNamedQuery("ProductItem.findByWarehouseVoucher")
                .setParameter("warehouseVoucher", warehouseVoucher)
                .getResultList();
        if (resultList != null) {
            productItemList = resultList;
        }
        return productItemList;
    }

    public List<ProductItem> findByWarehouseCode(String warehouseCode) {
        List<ProductItem> productItemList = new ArrayList<ProductItem>();
        List<ProductItem> resultList = getEntityManager().createNamedQuery("ProductItem.findByWarehouseCode")
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
        if (resultList != null) {
            productItemList = resultList;
        }
        return productItemList;
    }

    /**
     * Finds a list of ProductItems involved in a ProductItem List
     *
     * @param productItemList a given ProductItem List
     * @return a list of ProductItems involved in a ProductItem List
     */
    @SuppressWarnings(value = "unchecked")
    public List<ProductItem> findInProductItemList(List<ProductItem> productItemList) {
        List<ProductItem> resultProductItemList = new ArrayList<ProductItem>();
        List<ProductItem> resultList = getEntityManager().createNamedQuery("ProductItem.findInProductItemList")
                .setParameter("productItemList", productItemList)
                .getResultList();
        if (resultList != null) {
            resultProductItemList = resultList;
        }
        return resultProductItemList;
    }

    public ProductItem findProductItemByCode(String productItemCode) {
        ProductItem productItem = (ProductItem) em.createNamedQuery("ProductItem.findByCode")
                .setParameter("productItemCode", productItemCode)
                .getSingleResult();
        return productItem;
    }

    @Override
    public List<ArticulosPromocion> findArticuloCombo(ArticleOrder articulo) {
        Promocion promocion;
        try{
            promocion = (Promocion)em.createQuery("select promocion from Promocion promocion " +
                    " where promocion.invArticulos =:productItem")
            .setParameter("productItem",articulo.getProductItem())
            .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
        return new ArrayList<ArticulosPromocion>(promocion.getArticulosPromocions());
    }

    @SuppressWarnings(value = "unchecked")
    public BigDecimal getInitialInventoryYear(String productItemCode, String year){
        BigDecimal quantity = BigDecimal.ZERO;

        try {
            quantity = (BigDecimal)em.createNativeQuery("select i.cantidad " +
                                    "from inv_inicio i " +
                                    "where i.cod_art =:productItemCode " +
                                    "and i.gestion =:gestion")
                    .setParameter("productItemCode", productItemCode)
                    .setParameter("gestion", year)
                    .getSingleResult();
        }catch (NoResultException e){
            return BigDecimal.ZERO;
        }

        if (quantity == null)
            quantity = BigDecimal.ZERO;

        return  quantity;
    }

    public List<InventoryPeriod> getInventoryPeriodInitialList(String warehouseCode, String year){
        return (List<InventoryPeriod>)em.createQuery("select i from InventoryPeriod i " +
                                 "where i.warehouseCode =:warehouseCode " +
                                 "and i.year =:year " +
                                 "and i.month =:month ")
                .setParameter("warehouseCode", warehouseCode)
                .setParameter("year", new Integer(year))
                .setParameter("month", 1)
                .getResultList();
    }

    @Override
    public List<ProductItem> findProductItemList() {
        List<ProductItem> result = new ArrayList<ProductItem>();
        result = em.createQuery("select  p from ProductItem p")
                                    .getResultList();
        return result;
    }

    @Override
    public List<ProductItem> findBestProductList() {
        List<ProductItem> result = new ArrayList<ProductItem>();
        result = em.createQuery("select  p from ProductItem p " +
                " where p.fixSale =:fixSale " +
                " order by p.position asc")
                .setParameter("fixSale", Boolean.TRUE)
                .getResultList();
        return result;
    }

    @Override
    public EconomicActivity findEconomicActivity(String code){

        try {
            return (EconomicActivity)
                    em.createQuery("select e from EconomicActivity e " +
                            " where e.activityCode =:activityCode ")
                            .setParameter("activityCode", code)
                            .getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public ProductsServices findProductsAndServices(Integer code) {

        try {
            return (ProductsServices)
                    em.createQuery("select p from ProductsServices p " +
                            " where p.productCode =:productCode ")
                            .setParameter("productCode", code)
                            .getSingleResult();
        }catch (Exception e){
            return null;
        }

    }

    @Override
    public ProductsServices findProductsAndServices(String activityCode, Integer productCode) {

        try {
            return (ProductsServices)
                    em.createQuery("select p from ProductsServices p " +
                            " where p.activityCode =:activityCode " +
                            " and p.productCode =:productCode ")
                            .setParameter("activityCode", activityCode)
                            .setParameter("productCode", productCode)
                            .getSingleResult();
        }catch (Exception e){
            return null;
        }

    }

    @Override
    public MeasureUnitSIN findMeasureUnitSIN(Integer code) {
        try {
            return (MeasureUnitSIN)
                    em.createQuery("select p from MeasureUnitSIN p " +
                            " where p.code =:code ")
                            .setParameter("code", code)
                            .getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

}
