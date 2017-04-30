package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("rawMaterialProducerDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialProducerDataModel extends QueryDataModel<Long, RawMaterialProducer> {

    private static final String[] RESTRICTIONS = {
            "upper(rawMaterialProducer.firstName) like concat('%',concat(upper(#{rawMaterialProducerDataModel.criteria.firstName}), '%'))",
            "upper(rawMaterialProducer.lastName) like concat(upper(#{rawMaterialProducerDataModel.criteria.lastName}), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(upper(#{rawMaterialProducerDataModel.criteria.maidenName}), '%')",
            "upper(rawMaterialProducer.productiveZone.name) like concat('%', concat(upper(#{rawMaterialProducerDataModel.criteria.productiveZone.name}), '%'))",
            "upper(rawMaterialProducer.productiveZone.number) like concat('%',#{rawMaterialProducerDataModel.criteria.productiveZone.number}, '%')",
            "upper(rawMaterialProducer.productiveZone.group) like concat(#{rawMaterialProducerDataModel.criteria.productiveZone.group}, '%')"
    };

    @Create
    public void init() {
        sortProperty = "rawMaterialProducer.lastName";
    }

    @Override
    public String getEjbql() {
        String query = "select rawMaterialProducer " +
                "from RawMaterialProducer rawMaterialProducer " +
                "left join fetch rawMaterialProducer.productiveZone ";
        return query;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public RawMaterialProducer createInstance() {
        RawMaterialProducer rawMaterialProducer = super.createInstance();
        if (rawMaterialProducer.getProductiveZone() == null) {
            rawMaterialProducer.setProductiveZone(new ProductiveZone());
        }
        return rawMaterialProducer;
    }
}
