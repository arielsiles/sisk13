package com.encens.khipus.service.sales;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.sales.SalesOrder;
import com.encens.khipus.model.sales.SalesOrderDetail;
import com.encens.khipus.model.sales.SalesOrderState;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 1.0
 */
@Stateless
@Name("salesOrderService")
@AutoCreate
public class SalesOrderServiceBean extends GenericServiceBean implements SalesOrderService {

    @In
    private SalesOrderNumberGeneratorService salesOrderNumberGeneratorService;

    public SalesOrder createSalesOrder(SalesOrder salesOrder) throws EntryDuplicatedException {
        if (salesOrder.getCompanyNumber() == null) {
            salesOrder.setCompanyNumber(Constants.defaultCompanyNumber);
        }
        salesOrder.setDate(new Date());
        salesOrder.setState(SalesOrderState.BOR);
        salesOrder.setOrderNumber(salesOrderNumberGeneratorService.generateSalesOrderNumber(salesOrder));

        salesOrder.copyBuyerDataFromClient();
        prepareDetails(salesOrder);
        computeTotals(salesOrder);

        getEntityManager().persist(salesOrder);
        getEntityManager().flush();
        return salesOrder;
    }

    public void updateSalesOrder(SalesOrder salesOrder) throws EntryDuplicatedException {
        prepareDetails(salesOrder);
        computeTotals(salesOrder);
        if (!getEntityManager().contains(salesOrder)) {
            salesOrder = getEntityManager().merge(salesOrder);
        }
        getEntityManager().flush();
    }

    public void sendToReview(SalesOrder salesOrder) {
        salesOrder.setState(SalesOrderState.REV);
        salesOrder.setSentAt(new Date());
        getEntityManager().merge(salesOrder);
        getEntityManager().flush();
    }

    public void approve(SalesOrder salesOrder, String user) {
        salesOrder.setState(SalesOrderState.APR);
        salesOrder.setApprovedAt(new Date());
        salesOrder.setApprovedBy(user);
        getEntityManager().merge(salesOrder);
        getEntityManager().flush();
    }

    public void reject(SalesOrder salesOrder, String observations) {
        salesOrder.setState(SalesOrderState.BOR);
        salesOrder.setManagementObservations(observations);
        getEntityManager().merge(salesOrder);
        getEntityManager().flush();
    }

    public void nullify(SalesOrder salesOrder) {
        salesOrder.setState(SalesOrderState.ANL);
        salesOrder.setNullifiedAt(new Date());
        getEntityManager().merge(salesOrder);
        getEntityManager().flush();
    }

    public void computeTotals(SalesOrder salesOrder) {
        BigDecimal subTotal = BigDecimal.ZERO;
        if (salesOrder.getDetailList() != null) {
            for (SalesOrderDetail detail : salesOrder.getDetailList()) {
                BigDecimal lineTotal = BigDecimalUtil.multiply(
                        nz(detail.getQuantity()), nz(detail.getUnitPrice()), 2);
                detail.setTotalAmount(lineTotal);
                subTotal = BigDecimalUtil.sum(subTotal, lineTotal, 2);
            }
        }
        salesOrder.setSubTotalAmount(subTotal);
        BigDecimal total = BigDecimalUtil.sum(
                BigDecimalUtil.subtract(subTotal, nz(salesOrder.getDiscountAmount()), 2),
                nz(salesOrder.getRechargeAmount()), 2);
        salesOrder.setTotalAmount(total);
    }

    /**
     * Asigna numero de linea correlativo y la referencia inversa a la orden.
     */
    private void prepareDetails(SalesOrder salesOrder) {
        if (salesOrder.getDetailList() == null) {
            return;
        }
        long number = 1;
        for (SalesOrderDetail detail : salesOrder.getDetailList()) {
            detail.setSalesOrder(salesOrder);
            if (detail.getCompanyNumber() == null) {
                detail.setCompanyNumber(salesOrder.getCompanyNumber());
            }
            detail.setDetailNumber(number++);
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
