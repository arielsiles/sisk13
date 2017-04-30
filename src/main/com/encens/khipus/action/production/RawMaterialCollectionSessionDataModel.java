package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialCollectionSession;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("rawMaterialCollectionSessionDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialCollectionSessionDataModel extends QueryDataModel<Long, RawMaterialCollectionSession> {
    private final static String[] RESTRICTIONS = {
            "session.date >= #{rawMaterialCollectionSessionDataModel.privateCriteria.startDate}",
            "session.date <= #{rawMaterialCollectionSessionDataModel.privateCriteria.endDate}",
            "session.state = #{rawMaterialCollectionSessionDataModel.privateCriteria.state}",
            "upper(productiveZone.name) like concat(concat('%',upper(#{rawMaterialCollectionSessionDataModel.criteria.productiveZone.name})), '%')",
            "upper(productiveZone.group) like concat(concat('%',upper(#{rawMaterialCollectionSessionDataModel.criteria.productiveZone.group})), '%')",
            "productiveZone.number like concat(#{rawMaterialCollectionSessionDataModel.criteria.productiveZone.number}, '%')",
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{rawMaterialCollectionSessionDataModel.privateCriteria.rawMaterialProducer.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{rawMaterialCollectionSessionDataModel.privateCriteria.rawMaterialProducer.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{rawMaterialCollectionSessionDataModel.privateCriteria.rawMaterialProducer.maidenName})), '%')"
    };

    private PrivateCriteria privateCriteria;

    @Override
    public String getEjbql() {
        return "select distinct session " +
                "from RawMaterialCollectionSession session " +
                "left join fetch session.productiveZone productiveZone " +
                "left join session.collectedRawMaterialList collectedRawMaterial " +
                "left join collectedRawMaterial.rawMaterialProducer rawMaterialProducer";
    }

    @Override
    public String getSortProperty() {
        this.setSortAsc(false);
        return "session.date";
    }

    @Override
    public RawMaterialCollectionSession createInstance() {
        RawMaterialCollectionSession session = super.createInstance();
        session.setProductiveZone(new ProductiveZone());
        return session;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public PrivateCriteria getPrivateCriteria() {
        if (privateCriteria == null) {
            privateCriteria = new PrivateCriteria();
            privateCriteria.setState(ProductionCollectionState.PENDING);
        }
        return privateCriteria;
    }

    public static class PrivateCriteria {
        private Date startDate;
        private Date endDate;
        private RawMaterialProducer rawMaterialProducer = new RawMaterialProducer();
        private ProductionCollectionState state;

        public RawMaterialProducer getRawMaterialProducer() {
            return rawMaterialProducer;
        }

        public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
            this.rawMaterialProducer = rawMaterialProducer;
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

        public ProductionCollectionState getState() {
            return state;
        }

        public void setState(ProductionCollectionState state) {
            this.state = state;
        }
    }
}
