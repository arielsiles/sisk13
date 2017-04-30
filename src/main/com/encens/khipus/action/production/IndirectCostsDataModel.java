package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.IndirectCosts;
import com.encens.khipus.model.production.IndirectCostsConfig;
import com.encens.khipus.model.production.PeriodIndirectCost;
import com.encens.khipus.service.employees.GestionService;
import com.encens.khipus.service.production.PeriodIndirectCostService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Diego on 08/07/2014.
 */
@Name("indirectCostsDataModel")
@Scope(ScopeType.PAGE)
public class IndirectCostsDataModel extends QueryDataModel<Long,IndirectCosts> {
    @In
    private GestionService gestionService;

    @In
    private PeriodIndirectCostService periodIndirectCostService;

    private PrivateCriteria privateCriteria;

    @Factory(value = "monthEnumIndirectCosts")
    public Month[] getMonthEnum() {
        return Month.values();
    }


    private static final String[] RESTRICTIONS = {
             "upper(indirectCosts.name) like concat(concat('%',upper( #{indirectCostsDataModel.criteria.name})), '%')"
            ," indirectCosts.periodIndirectCost = #{indirectCostsDataModel.privateCriteria.periodIndirectCost}"
            //,"indirectCosts.periodIndirectCost.month = #{indirectCostsDataModel.privateCriteria.monthInt}"
    };

    @Override
    public String getEjbql(){
        return " select indirectCosts from IndirectCosts indirectCosts " +
               " where indirectCosts.periodIndirectCost is not null";
    }

    @Override
    public void search() {
        privateCriteria.refreshPeriodIndirectCost();
        super.search();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PrivateCriteria getPrivateCriteria() {
        if (privateCriteria == null) {
            privateCriteria = new PrivateCriteria();
            privateCriteria.setPeriodIndirectCostService(periodIndirectCostService);
            privateCriteria.setPeriodIndirectCost(periodIndirectCostService.findLastPeriodIndirectCost());
            privateCriteria.setGestion(privateCriteria.getPeriodIndirectCost().getGestion());
            privateCriteria.setMonth(Month.getMonth(privateCriteria.getPeriodIndirectCost().getMonth() - 1));

        }
        return privateCriteria;
    }

    public static class PrivateCriteria {

        private PeriodIndirectCostService periodIndirectCostService;

        private Gestion gestion;

        private Month month;

        private PeriodIndirectCost periodIndirectCost;

        public Month getMonth() {
            return month;
        }

        public void setMonth(Month month) {
            if(month != null && gestion != null )
            this.periodIndirectCost = periodIndirectCostService.findPeriodIndirect(month,gestion);
            this.month = month;
        }

        public void setMonth(int month) {

            this.month = Month.getMonth(month);
        }

        public Gestion getGestion() {
            return gestion;
        }

        public void setGestion(Gestion gestion) {
            if(month != null && gestion != null )
            this.periodIndirectCost = periodIndirectCostService.findPeriodIndirect(month,gestion);
            this.gestion = gestion;
        }

        public void cleanGestionList() {
            setGestion(null);
        }


        public PeriodIndirectCost getPeriodIndirectCost() {
            return periodIndirectCost;
        }

        public void setPeriodIndirectCost(PeriodIndirectCost periodIndirectCost) {
            this.periodIndirectCost = periodIndirectCost;
        }

        public PeriodIndirectCostService getPeriodIndirectCostService() {
            return periodIndirectCostService;
        }

        public void setPeriodIndirectCostService(PeriodIndirectCostService periodIndirectCostService) {
            this.periodIndirectCostService = periodIndirectCostService;
        }

        public void refreshPeriodIndirectCost() {
            this.periodIndirectCost = periodIndirectCostService.findPeriodIndirect(month,gestion);
        }
    }
}
