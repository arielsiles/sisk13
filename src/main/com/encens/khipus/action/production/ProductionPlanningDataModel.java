package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionPlanning;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/20/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("productionPlanningDataModel")
@Scope(ScopeType.PAGE)
public class ProductionPlanningDataModel extends QueryDataModel<Long, ProductionPlanning> {

    private ModelCriteria modelCriteria;

    private static final String[] RESTRICTIONS = {
            "productionPlanning.date >= #{productionPlanningDataModel.modelCriteria.startDate}",
            "productionPlanning.date <= #{productionPlanningDataModel.modelCriteria.endDate}",
            "upper(productionOrder.code) like concat(concat('%',upper(#{productionPlanningDataModel.modelCriteria.order})), '%')"
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
        return "select DISTINCT productionPlanning " +
                " from ProductionPlanning productionPlanning " +
                " left join fetch productionPlanning.productionOrderList productionOrder " ;
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

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public Date getStartDate() {
           /* Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,2014);
            calendar.set(Calendar.MONTH,Calendar.APRIL);
            calendar.set(Calendar.DAY_OF_MONTH,30);
            return calendar.getTime();*/
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {

            /*Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,2014);
            calendar.set(Calendar.MONTH,Calendar.MAY);
            calendar.set(Calendar.DAY_OF_MONTH,31);
            return calendar.getTime();*/
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }
    }
}
