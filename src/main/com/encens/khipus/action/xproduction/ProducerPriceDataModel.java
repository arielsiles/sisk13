package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.xproduction.ProducerPrice;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("producerPriceDataModel")
@Scope(ScopeType.PAGE)
public class ProducerPriceDataModel extends QueryDataModel<Long, ProducerPrice> {

    private String firstName;
    private String lastName;
    private MetaProduct metaProduct;

    private static final String[] RESTRICTIONS = {
            "upper(producerPrice.rawMaterialProducer.firstName) like concat(concat('%',upper(#{producerPriceDataModel.firstName})), '%')",
            "upper(producerPrice.rawMaterialProducer.lastName) like concat(concat('%',upper(#{producerPriceDataModel.lastName})), '%')",
            "producerPrice.metaProduct = #{producerPriceDataModel.metaProduct}"
    };

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select producerPrice from ProducerPrice producerPrice";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }
}
