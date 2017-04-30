package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("salaryMovementGABDataModel")
@Scope(ScopeType.PAGE)
public class SalaryMovementGABDataModel extends QueryDataModel<Long, SalaryMovementGAB> {

    private PrivateCriteria privateCriteria;

    private static final String[] RESTRICTIONS = {
            "salaryMovementGAB.date >= #{salaryMovementGABDataModel.privateCriteria.startDate}",
            "salaryMovementGAB.date <= #{salaryMovementGABDataModel.privateCriteria.endDate}",
            "salaryMovementGAB.state = #{salaryMovementGABDataModel.privateCriteria.state}",
            "upper(productiveZone.number) like concat(upper(concat('%',#{salaryMovementGABDataModel.criteria.productiveZone.number})), '%')",
            "upper(productiveZone.group) like concat(upper(concat('%',#{salaryMovementGABDataModel.criteria.productiveZone.group})), '%')",
            "upper(productiveZone.name) like concat(upper(concat('%',#{salaryMovementGABDataModel.criteria.productiveZone.name})), '%')",
    };

    @Create
    public void init() {
        sortProperty = "salaryMovementGAB.date";
    }

    @Override
    public String getEjbql() {
        String query = " select salaryMovementGAB " +
                       " from SalaryMovementGAB salaryMovementGAB " +
                       " left join fetch salaryMovementGAB.productiveZone productiveZone";
        return query;
    }

    @Override
    public SalaryMovementGAB createInstance() {
        SalaryMovementGAB salaryMovementGAB = super.createInstance();
        salaryMovementGAB.setProductiveZone(new ProductiveZone());
        return salaryMovementGAB;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public static class PrivateCriteria{
        private Date startDate;
        private Date endDate;
        private ProductionCollectionState state;

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

        public ProductionCollectionState getState() {
            return state;
        }

        public void setState(ProductionCollectionState state) {
            this.state = state;
        }
    }

    public PrivateCriteria getPrivateCriteria() {
        if (privateCriteria == null) {
            privateCriteria = new PrivateCriteria();
            privateCriteria.setState(ProductionCollectionState.PENDING);
        }
        return privateCriteria;
    }

    public void setPrivateCriteria(PrivateCriteria privateCriteria) {
        this.privateCriteria = privateCriteria;
    }

}
