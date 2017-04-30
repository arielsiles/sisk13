package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;
import com.encens.khipus.model.warehouse.WarehouseState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */
@Name("productionOrderSearchDataModel")
@Scope(ScopeType.PAGE)
public class ProductionOrderSearchDataModel extends QueryDataModel<Long,ProductionOrder> {
    private String code;
    private String name;
    private Date date;

    private static final String[] RESTRICTIONS = {
            "lower(productionOrder.productComposition.processedProduct.name) like concat('%', concat(lower(#{productionOrderSearchDataModel.name}), '%'))",
            "productionOrder.code = #{productionOrderSearchDataModel.code}",
            "productionOrder.productionPlanning.date = #{productionPlanningAction.instance.date}"
    };

    @Create
    public void init() {
        sortProperty = "productionOrder.code";
    }

    @Override
    public String getEjbql() {
        return  " select productionOrder from ProductionOrder productionOrder " +
                " where productionOrder.productMain is null";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear(){
        code= null;
        name = null;
        super.clear();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
