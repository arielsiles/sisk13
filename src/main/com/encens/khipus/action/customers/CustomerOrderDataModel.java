package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.SaleTypeEnum;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * ClientDataModel
 *
 * @author
 * @version 2.0
 */
@Name("customerOrderDataModel")
@Scope(ScopeType.PAGE)
public class CustomerOrderDataModel extends QueryDataModel<Long, CustomerOrder> {

    private String name;
    private String lastName;
    private String maidenName;
    private SaleTypeEnum saleType;
    private Integer invoice;
    private Date initDate;
    private Date endDate;

    private static final String[] RESTRICTIONS = {
            "customerOrder.saleType = #{customerOrderDataModel.saleType}",
            "customerOrder.movement.number = #{customerOrderDataModel.invoice}",
            "customerOrder.orderDate >= #{customerOrderDataModel.initDate}",
            "customerOrder.orderDate <= #{customerOrderDataModel.endDate}",
            "lower(customerOrder.code) like concat('%', concat(lower(#{customerOrderDataModel.criteria.code}), '%'))",
            "lower(customerOrder.client.name) like concat('%', concat(lower(#{customerOrderDataModel.name}), '%'))",
            "lower(customerOrder.client.lastName) like concat('%', concat(lower(#{customerOrderDataModel.lastName}), '%'))",
            "lower(customerOrder.client.maidenName) like concat('%', concat(lower(#{customerOrderDataModel.maidenName}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "customerOrder.orderDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select customerOrder from CustomerOrder customerOrder";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public List<CustomerOrder> getSelectedCustomerOrdes() {
        List ids = super.getSelectedIdList();

        List<CustomerOrder> result = new ArrayList<CustomerOrder>();
        for (Object id : ids) {
            CustomerOrder item = getEntityManager().find(CustomerOrder.class, id);
            result.add(item);
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }

    public Integer getInvoice() {
        return invoice;
    }

    public void setInvoice(Integer invoice) {
        this.invoice = invoice;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
