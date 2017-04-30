package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.ProductionPlanning;
import com.encens.khipus.model.production.ProductionPlanningState;
import com.encens.khipus.model.warehouse.ProductionTransferLog;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("productionTransferLogDataModel")
@Scope(ScopeType.PAGE)
public class ProductionTransferLogDataModel extends QueryDataModel<Long, ProductionPlanning> {
    private ModelCriteria modelCriteria;

    private static final String[] RESTRICTIONS = {
            "productionPlanning.date >= #{productionTransferLogDataModel.modelCriteria.startDate}",
            "productionPlanning.date <= #{productionTransferLogDataModel.modelCriteria.endDate}",
            "productionOrder.estateOrder = #{productionTransferLogDataModel.modelCriteria.STATEFINALIZED} ",
           // " OR productionOrder.estateOrder = #{productionTransferLogDataModel.modelCriteria.STATEINSTOCK}"+
           // " OR productionOrder.estateOrder = #{productionTransferLogDataModel.modelCriteria.STATETABULATE}",
            "upper(productionOrder.code) like concat(concat('%',upper(#{productionTransferLogDataModel.modelCriteria.order})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "productionPlanning.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        /*return "select productionPlanning " +
                "from ProductionPlanning productionPlanning " +
                "left join fetch productionPlanning.productionOrderList productionOrder " +
                "left join fetch productionOrder.productComposition productComposition " +
                "left join fetch productComposition.processedProduct ";*/
        return "select distinct productionPlanning " +
                "from ProductionPlanning productionPlanning " +
                "inner join productionPlanning.productionOrderList productionOrder ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public ModelCriteria getModelCriteria() {
        if (modelCriteria == null) {
            modelCriteria = new ModelCriteria();
        }
        return modelCriteria;
    }

    public void setModelCriteria(ModelCriteria modelCriteria) {
        this.modelCriteria = modelCriteria;
    }

    public List<ProductionPlanning> getListProductoPlannig(){
        return this.getList(1,getCount().intValue());
    }

    public static class ModelCriteria {
        private String order;
        private Date startDate;
        private Date endDate;

        private ProductionPlanningState STATEFINALIZED = ProductionPlanningState.FINALIZED;
        private ProductionPlanningState STATEINSTOCK = ProductionPlanningState.INSTOCK;
        private ProductionPlanningState STATETABULATE = ProductionPlanningState.TABULATED;

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
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

        public ProductionPlanningState getSTATEFINALIZED() {
            return STATEFINALIZED;
        }

        public void setSTATEFINALIZED(ProductionPlanningState STATEFINALIZED) {
            this.STATEFINALIZED = STATEFINALIZED;
        }

        public ProductionPlanningState getSTATEINSTOCK() {
            return STATEINSTOCK;
        }

        public void setSTATEINSTOCK(ProductionPlanningState STATEINSTOCK) {
            this.STATEINSTOCK = STATEINSTOCK;
        }

        public ProductionPlanningState getSTATETABULATE() {
            return STATETABULATE;
        }

        public void setSTATETABULATE(ProductionPlanningState STATETABULATE) {
            this.STATETABULATE = STATETABULATE;
        }
    }
}
