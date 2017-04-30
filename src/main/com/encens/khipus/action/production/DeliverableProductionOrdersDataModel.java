package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductionOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("deliverableProductionOrdersDataModel")
@Scope(ScopeType.PAGE)
public class DeliverableProductionOrdersDataModel extends QueryDataModel<Long, ProductionOrder> {
    private PrivateCriteria privateCriteria;

    private final static String[] RESTRICTIONS = {
            "upper(productionOrder.code) like concat(concat('%',upper(#{deliverableProductionOrdersDataModel.criteria.code})), '%')",
            "upper(processedProduct.name) like concat(concat('%',upper(#{deliverableProductionOrdersDataModel.privateCriteria.processedProduct.name})), '%')",
            "productionPlanning.date >= #{deliverableProductionOrdersDataModel.privateCriteria.startDate}",
            "productionPlanning.date <= #{deliverableProductionOrdersDataModel.privateCriteria.endDate}"
    };

    @Override
    public String getEjbql() {
        return "select outputProductionVoucher " +
                "from ProductionOrder productionOrder " +
                "join productionOrder.outputProductionVoucherList outputProductionVoucher " +
                "join fetch outputProductionVoucher.processedProduct processedProduct " +
                "join productionOrder.productComposition productComposition " +
                "join productionOrder.productionPlanning productionPlanning " +
                "where outputProductionVoucher.incomingProductionOrder is null " +
                "and productionPlanning.state = com.encens.khipus.model.production.ProductionPlanningState.FINALIZED";
    }

    @Create
    public void defaultSort() {
        sortProperty = "productionOrder.code";
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
        private Date startDate;
        private Date endDate;
        private ProcessedProduct processedProduct;

        public PrivateCriteria() {
            processedProduct = new ProcessedProduct();
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

        public ProcessedProduct getProcessedProduct() {
            return processedProduct;
        }

        public void setProcessedProduct(ProcessedProduct processedProduct) {
            this.processedProduct = processedProduct;
        }
    }
}
