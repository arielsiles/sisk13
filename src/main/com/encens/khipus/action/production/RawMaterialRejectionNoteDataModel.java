package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.RawMaterialRejectionNote;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("rawMaterialRejectionNoteDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialRejectionNoteDataModel extends QueryDataModel<Long, RawMaterialRejectionNote> {
    private final static String[] RESTRICTIONS = {
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{rawMaterialProducerForSearch.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{rawMaterialProducerForSearch.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{rawMaterialProducerForSearch.maidenName})), '%')",
            "upper(productiveZone.name) like concat(concat('%',upper(#{productiveZoneForSearch.name})), '%')",
            "upper(productiveZone.group) like concat(concat('%',upper(#{productiveZoneForSearch.group})), '%')",
            "upper(productiveZone.number) like concat(#{productiveZoneForSearch.number}, '%')",
            "rawMaterialRejectionNote.state = #{rawMaterialRejectionNoteDataModel.state}"
    };

    private ProductionCollectionState state;

    @Factory(value = "productiveZoneForSearch", scope = ScopeType.PAGE)
    public ProductiveZone initProductiveZone() {
        return new ProductiveZone();
    }

    @Factory(value = "rawMaterialProducerForSearch", scope = ScopeType.PAGE)
    public RawMaterialProducer initRawMaterialProducer() {
        return new RawMaterialProducer();
    }

    @Create
    public void init() {
        this.state = ProductionCollectionState.PENDING;
        sortProperty = "rawMaterialRejectionNote.rawMaterialProducer.lastName";
    }

    @Override
    public String getEjbql() {
        return "select rawMaterialRejectionNote " +
                "from RawMaterialRejectionNote rawMaterialRejectionNote " +
                "left join fetch rawMaterialRejectionNote.rawMaterialProducer rawMaterialProducer " +
                "left join fetch rawMaterialProducer.productiveZone productiveZone";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public ProductionCollectionState getState() {
        return state;
    }

    public void setState(ProductionCollectionState state) {
        this.state = state;
    }
}
