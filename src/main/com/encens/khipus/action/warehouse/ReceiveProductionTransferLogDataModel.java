package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.warehouse.ProductionTransferLog;
import com.encens.khipus.model.warehouse.ProductionTransferLogState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("receiveProductionTransferLogDataModel")
@Scope(ScopeType.PAGE)
public class ReceiveProductionTransferLogDataModel extends QueryDataModel<Long, ProductionTransferLog> {
    private PrivateCriteria privateCriteria;

    private final static String[] RESTRICTIONS = {
            "productionOrder.code like concat(#{receiveProductionTransferLogDataModel.privateCriteria.productionOrder.code})",
            "productionTransferLog.deliveredDate >= #{receiveProductionTransferLogDataModel.privateCriteria.startDate}",
            "productionTransferLog.deliveredDate <= #{receiveProductionTransferLogDataModel.privateCriteria.endDate}",
            "productionTransferLog.state = #{receiveProductionTransferLogDataModel.criteria.state}",
            "productionTransferLog.state != #{receiveProductionTransferLogDataModel.privateCriteria.excludedState}"
    };

    @Create
    public void init() {
        sortProperty = "productionTransferLog.deliveredDate";
    }

    @Override
    public String getEjbql() {
        return "select distinct productionTransferLog " +
                "from ProductionTransferLog productionTransferLog " +
                "left join productionTransferLog.incomingProductionOrderList incomingProductionOrder " +
                "left join incomingProductionOrder.outputProductionVoucher outputProductionVoucher " +
                "left join outputProductionVoucher.productionOrder productionOrder ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PrivateCriteria getPrivateCriteria() {
        if (privateCriteria == null) {
            privateCriteria = new PrivateCriteria();
        }
        return privateCriteria;
    }

    public static class PrivateCriteria {
        private ProductionOrder productionOrder = new ProductionOrder();
        private Date startDate;
        private Date endDate;
        private ProductionTransferLogState excludedState = ProductionTransferLogState.DRAFT;

        public ProductionOrder getProductionOrder() {
            return productionOrder;
        }

        public void setProductionOrder(ProductionOrder productionOrder) {
            this.productionOrder = productionOrder;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public ProductionTransferLogState getExcludedState() {
            return excludedState;
        }
    }
}
