package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.xproduction.ProducerPrice;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("producerPriceAction")
@Scope(ScopeType.CONVERSATION)
public class ProducerPriceAction extends GenericAction<ProducerPrice> {

    @Factory(value = "producerPrice", scope = ScopeType.STATELESS)
    public ProducerPrice initContinent() {
        return getInstance();
    }

    public void assignSupplier(RawMaterialProducer supplier){
        getInstance().setRawMaterialProducer(supplier);
    }

    public void clearRawMaterialProducer() {
        getInstance().setRawMaterialProducer(null);
    }

}
