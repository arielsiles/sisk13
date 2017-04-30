package com.encens.khipus.action.warehouse;

import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.purchases.PurchaseOrderDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.service.warehouse.WarehousePurchaseOrderDetailService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the logic to add or remove the <code>PurchaseOrderDetail</code> objects, the purchase order details
 * are added one by one and are stored in a <code>List</code> object they are used to show the selected elements
 * in the user view.
 * <p/>
 * The user can add a <code>PurchaseOrderDetail</code> only when the provider its defined in the
 * <code>PurchaseOrder</code> object. So if the provider is changed in the <code>PurchaseOrder</code>, the
 * <code>List</code> that contain the added <code>PurchaseOrderDetail</code> objects will be clean.
 * <p/>
 * Also implements the logic to calculate the total amount and calculate the unit cost for every
 * <code>PurchaseOrderDetail</code> that was added.
 *
 * @author
 * @version 2.23
 */
@Name("warehousePurchaseOrderDetailListCreateAction")
@Scope(ScopeType.CONVERSATION)
public class WarehousePurchaseOrderDetailListCreateAction implements Serializable {

    private List<PurchaseOrderDetail> instances = new ArrayList<PurchaseOrderDetail>();

    private List<ProductItemPK> selectedProductItems = new ArrayList<ProductItemPK>();

    @Logger
    private Log log;

    @In
    private FacesMessages facesMessages;

    @In
    private WarehousePurchaseOrderDetailService warehousePurchaseOrderDetailService;

    @In(value = "warehousePurchaseOrderAction")
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    public void removeInstance(PurchaseOrderDetail instance) {
        instances.remove(instance);
        selectedProductItems.remove(instance.getProductItem().getId());
    }

    public void addProductItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            if (selectedProductItems.contains(productItem.getId())) {
                continue;
            }

            selectedProductItems.add(productItem.getId());

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setProductItem(productItem);

            detail.setPurchaseOrder(warehousePurchaseOrderAction.getPurchaseOrder());
            putDefaultValuesForSelectedProductItem(detail);
            instances.add(detail);
        }
    }

    public void putDefaultValuesForSelectedProductItem(PurchaseOrderDetail instance) {
        Provide provide = warehousePurchaseOrderDetailService.findProvideElement(instance.getProductItem(),
                warehousePurchaseOrderAction.getInstance().getProvider());
        setDefaultValuesForSelectedProductItem(instance, provide);
    }

    /**
     * Put the default provide values in the <code>PurchaseOrderDetail</code> object also calculate the <code>totalAmount</code>
     * value.
     *
     * @param instance The <code>PurchaseOrderDetail</code> object where the values are updated.
     * @param provide  The <code>Provide</code> object.
     */
    public void setDefaultValuesForSelectedProductItem(PurchaseOrderDetail instance, Provide provide) {
        if (null == provide.getGroupMeasureUnit()) {
            throw new RuntimeException("Cannot execute the setDefaultValuesForSelectedProductItem(...) method, "
                    + "because the Provide id: "
                    + provide.getId()
                    + " have not assigned a groupMeasureUnit.");
        }

        instance.setUnitCost(provide.getGroupAmount());
        instance.setPurchaseMeasureUnit(provide.getGroupMeasureUnit());
        updateTotalAmount(instance);
    }

    /**
     * Calculate the unit cost field in the <code>PurchaseOrderDetail</code> object, the required elements to make de
     * calculation are: <code>requestedQuantity</code> and <code>totalAmount</code>.
     *
     * @param instance <code>PurchaseOrderDetail</code> object where the <code>unitCost</code> value will be
     *                 updated and also contains the <code>requestedQuantity</code>, <code>totalAmount</code> values.
     */
    public void updateUnitCost(PurchaseOrderDetail instance) {
        if (null != instance.getRequestedQuantity() && null != instance.getTotalAmount()) {
            if (instance.getRequestedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal unitCost = BigDecimalUtil.divide(instance.getTotalAmount(), instance.getRequestedQuantity());
                instance.setUnitCost(unitCost);
            } else {
                Provide provide = warehousePurchaseOrderDetailService.findProvideElement(
                        instance.getProductItem(),
                        warehousePurchaseOrderAction.getInstance().getProvider());
                instance.setUnitCost(provide.getGroupAmount());
            }
        }
    }

    /**
     * Calculate the total amount field in the <code>PurchaseOrderDetail</code> object, the required elements to make de
     * calculation are: <code>requestedQuantity</code> and <code>unitCost</code>.
     *
     * @param instance <code>PurchaseOrderDetail</code> object where the <code>totalAmount</code> value will be
     *                 updated and also contains the <code>requestedQuantity</code>, <code>unitCost</code> values.
     */
    public void updateTotalAmount(PurchaseOrderDetail instance) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        log.debug("instance.getRequestedQuantity(): " + instance.getRequestedQuantity());
        log.debug("instance.getUnitCost(): " + instance.getUnitCost());
        if (null != instance.getRequestedQuantity() && null != instance.getUnitCost()) {
            totalAmount = BigDecimalUtil.multiply(instance.getRequestedQuantity(), instance.getUnitCost(), 6);
        }

        instance.setTotalAmount(totalAmount);
    }

    public void initializeAction() {
        instances = new ArrayList<PurchaseOrderDetail>();
        selectedProductItems = new ArrayList<ProductItemPK>();
    }

    public List<PurchaseOrderDetail> getInstances() {
        return instances;
    }

    public void setInstances(List<PurchaseOrderDetail> instances) {
        this.instances = instances;
    }

    public Integer getRows() {
        if (null != instances && !instances.isEmpty()) {
            return instances.size() + 1;
        }

        return 1;
    }
}
