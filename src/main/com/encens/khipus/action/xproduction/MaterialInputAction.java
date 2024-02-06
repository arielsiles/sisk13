package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.xproduction.MaterialInput;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("materialInputAction")
@Scope(ScopeType.CONVERSATION)
public class MaterialInputAction extends GenericAction<MaterialInput> {

    @Factory(value = "materialInput", scope = ScopeType.STATELESS)
    public MaterialInput initMaterialInput() {
        return getInstance();
    }

    /*@Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Formulation instance) {
        String outCome = super.select(instance);
        setOp(OP_UPDATE);
        setFormulationInputList(getInstance().getFormulationInputList());
        return outCome;
    }*/

    /*@Override
    public String update() {

        formulationService.updateFormulation(getInstance(), formulationInputList);
        return Outcome.SUCCESS;
    }*/

    public void assignProduct(ProductItem productItem){
        getInstance().setProductItem(productItem);
        getInstance().setProductItemCode(productItem.getProductItemCode());
    }

    public void assignMaterial(ProductItem productItem){
        getInstance().setProductItemMaterial(productItem);
        getInstance().setProductItemMaterialCode(productItem.getProductItemCode());
    }

    public void clearProductItem(){
        getInstance().setProductItem(null);
        getInstance().setProductItemCode(null);
    }

    public void clearProductItemMaterial(){
        getInstance().setProductItemMaterial(null);
        getInstance().setProductItemMaterialCode(null);
    }

    public String isDefaultQuantity(MaterialInput materialInput){
        String result = "NO";
        if (materialInput.getQuantityFlag())
            result = "SI";

        return result;
    }

}
