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
@Name("salaryMovementProducerDataModel")
@Scope(ScopeType.PAGE)
public class SalaryMovementProducerDataModel extends QueryDataModel<Long, SalaryMovementProducer> {

    private PrivateCriteria privateCriteria;

    private static final String[] RESTRICTIONS = {
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.privateCriteria.startDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.privateCriteria.endDate}",
            "salaryMovementProducer.state = #{salaryMovementProducerDataModel.privateCriteria.state}",
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.maidenName})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "salaryMovementProducer.date";
    }

    @Override
    public String getEjbql() {
        String query = " select salaryMovementProducer " +
                       " from SalaryMovementProducer salaryMovementProducer " +
                       " left join fetch salaryMovementProducer.rawMaterialProducer rawMaterialProducer";
        return query;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public SalaryMovementProducer createInstance() {
        SalaryMovementProducer salaryMovementGAB = super.createInstance();
        salaryMovementGAB.setRawMaterialProducer(new RawMaterialProducer());
        return salaryMovementGAB;
    }

    public static class PrivateCriteria{
        private Date startDate;
        private Date endDate;
        private ProductionCollectionState state;

        public ProductionCollectionState getState() {
            return state;
        }

        public void setState(ProductionCollectionState state) {
            this.state = state;
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
