package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.VentaDirecta;
import com.encens.khipus.model.warehouse.ProductInventory;
import com.encens.khipus.model.warehouse.ProductInventoryRecord;
import com.encens.khipus.model.warehouse.ProductInventoryRecordType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 */

@Stateless
@Name("productInventoryService")
@AutoCreate
public class ProductInventoryServiceBean extends GenericServiceBean implements ProductInventoryService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public ProductInventory findProductInventoryByCode(String productItemCode) {
        try {
            return (ProductInventory) em.createNamedQuery("ProductInventory.findByCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }

    @Override
    public void inputProducts(String productItemCode, Double quantity) {

        ProductInventory productInventory = findProductInventoryByCode(productItemCode);
        Double currentAmount = productInventory.getQuantity().doubleValue() + quantity;
        productInventory.setQuantity(BigDecimal.valueOf(currentAmount));

        ProductInventoryRecord productInventoryRecord = new ProductInventoryRecord();
        productInventoryRecord.setProductItemCode(productItemCode);
        productInventoryRecord.setQuantity(BigDecimal.valueOf(quantity));
        productInventoryRecord.setType(ProductInventoryRecordType.PRODUCTION);
        productInventoryRecord.setMovementType(ProductInventoryRecordType.PRODUCTION.getType());
        productInventoryRecord.setDescription(productInventory.getProductItem().getName());
        productInventoryRecord.setProductInventory(productInventory);

        em.persist(productInventoryRecord);
        em.flush();


    }

    @Override
    public void outputProducts(VentaDirecta directSales) {

        for ( ArticleOrder articleOrder : directSales.getArticulosPedidos()){
            ProductInventory productInventory = findProductInventoryByCode(articleOrder.getCodArt());
            Double stock = productInventory.getQuantity().doubleValue();
            stock = stock - articleOrder.getTotal().doubleValue();
            productInventory.setQuantity(BigDecimal.valueOf(stock));

            ProductInventoryRecord productInventoryRecord = new ProductInventoryRecord();
            productInventoryRecord.setProductItemCode(articleOrder.getCodArt());
            productInventoryRecord.setQuantity(BigDecimal.valueOf(articleOrder.getTotal()));
            productInventoryRecord.setType(ProductInventoryRecordType.DELIVERY_CASH_SALE);
            productInventoryRecord.setMovementType(ProductInventoryRecordType.DELIVERY_CASH_SALE.getType());
            productInventoryRecord.setDescription(productInventory.getProductItem().getFullName()+", #" + directSales.getCodigo());
            productInventoryRecord.setProductInventory(productInventory);

            productInventory.getProductInventoryRecords().add(productInventoryRecord);
            directSales.setStockFlag(true);

            em.persist(productInventoryRecord);
            em.flush();
        }
    }

    @Override
    public void outputProducts(CustomerOrder customerOrder) {

        for ( ArticleOrder articleOrder : customerOrder.getArticulosPedidos() ){
            ProductInventory productInventory = findProductInventoryByCode(articleOrder.getCodArt());
            System.out.println("---> Articulo: " + productInventory.getProductItem().getName());
            Double stock = productInventory.getQuantity().doubleValue();
            stock = stock - articleOrder.getTotal().doubleValue();
            productInventory.setQuantity(BigDecimal.valueOf(stock));

            ProductInventoryRecord productInventoryRecord = new ProductInventoryRecord();
            productInventoryRecord.setProductItemCode(articleOrder.getCodArt());
            productInventoryRecord.setQuantity(BigDecimal.valueOf(articleOrder.getTotal()));
            productInventoryRecord.setType(ProductInventoryRecordType.DELIVERY_CUSTOMER_ORDER);
            productInventoryRecord.setMovementType(ProductInventoryRecordType.DELIVERY_CASH_SALE.getType());
            productInventoryRecord.setDescription(productInventory.getProductItem().getFullName()+", #" + customerOrder.getCodigo());
            productInventoryRecord.setProductInventory(productInventory);

            productInventory.getProductInventoryRecords().add(productInventoryRecord);
            customerOrder.setStockFlag(true);

            em.persist(productInventoryRecord);
            em.flush();

            System.out.println(".....OUPUT CUSTOMER ORDER.... " + articleOrder.getCodArt() + ": " + stock);
        }
    }

    public void outputProducts(List<CustomerOrder> customerOrders){
        for ( CustomerOrder customerOrder : customerOrders ){
            if (!customerOrder.getStockFlag())
                outputProducts(customerOrder);
        }
    }

    public void updateProductInventory(String productItemCode, Double quantity, ProductInventoryRecordType typeRecord, String description){

        ProductInventory productInventory = findProductInventoryByCode(productItemCode);
        Double stock = productInventory.getQuantity().doubleValue();

        if (typeRecord.getType().equals("INPUT"))
            stock = stock + quantity;
        else
            stock = stock - quantity;

        productInventory.setQuantity(BigDecimal.valueOf(stock));

        ProductInventoryRecord productInventoryRecord = new ProductInventoryRecord();
        productInventoryRecord.setProductItemCode(productItemCode);
        productInventoryRecord.setQuantity(BigDecimal.valueOf(quantity));
        productInventoryRecord.setType(typeRecord);
        productInventoryRecord.setMovementType(typeRecord.getType());
        productInventoryRecord.setDescription(typeRecord.getType() + " " + productInventory.getProductItem().getFullName());
        productInventoryRecord.setProductInventory(productInventory);

        productInventory.getProductInventoryRecords().add(productInventoryRecord);

        em.persist(productInventoryRecord);
        em.flush();

    }

}
