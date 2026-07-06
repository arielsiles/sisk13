package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.sales.SalesOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model para la lista de Ordenes de Venta.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALESORDER','VIEW')}")
public class SalesOrderDataModel extends QueryDataModel<Long, SalesOrder> {

    private static final String[] RESTRICTIONS = {
            "lower(salesOrder.orderNumber) like concat(lower(#{salesOrderDataModel.criteria.orderNumber}), '%')",
            "lower(salesOrder.buyerName) like concat('%', concat(lower(#{salesOrderDataModel.criteria.buyerName}), '%'))",
            "salesOrder.state = #{salesOrderDataModel.state}",
            "salesOrder.date >= #{salesOrderDataModel.startDate}",
            "salesOrder.date <= #{salesOrderDataModel.endDate}"};

    /** Estado como campo propio (null = Todos). No se usa criteria.state porque la
     *  entidad inicializa state=BOR y filtraria por Borrador por defecto. */
    private com.encens.khipus.model.sales.SalesOrderState state;
    private java.util.Date startDate;
    private java.util.Date endDate;

    public com.encens.khipus.model.sales.SalesOrderState getState() {
        return state;
    }

    public void setState(com.encens.khipus.model.sales.SalesOrderState state) {
        this.state = state;
    }

    @Create
    public void init() {
        sortProperty = "salesOrder.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select salesOrder from SalesOrder salesOrder" +
                " left join fetch salesOrder.client" +
                " left join fetch salesOrder.incoterm";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public java.util.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }
}
