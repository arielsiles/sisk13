package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.warehouse.ProductItemMinimalStockIsGreaterThanMaximumStockException;
import com.encens.khipus.exception.warehouse.ProductItemNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.ArticulosPromocion;
import com.encens.khipus.model.customers.Promocion;
import com.encens.khipus.model.customers.Ventaarticulo;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
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
        } catch (PersistenceException e) {
            throw new EntryDuplicatedException();
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

}
