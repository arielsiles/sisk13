package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.Formulation;
import com.encens.khipus.model.production.FormulationInput;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;


@Name("formulationAction")
@Scope(ScopeType.CONVERSATION)
public class FormulationAction extends GenericAction<Formulation> {

    private List<FormulationInput> formulationInputList = new ArrayList<FormulationInput>();

    @Factory(value = "formulation", scope = ScopeType.STATELESS)
    public Formulation initProductionPlan() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Formulation instance) {
        String outCome = super.select(instance);
        setOp(OP_UPDATE);
        setFormulationInputList(getInstance().getFormulationInputList());
        return outCome;
    }


    public void removeInput(FormulationInput formulationInput){

    }


    public void addIngredientItems(List<ProductItem> productItems) {
        for (ProductItem productItem : productItems) {
            FormulationInput input = new FormulationInput();
            input.setProductItemCode(productItem.getProductItemCode());
            input.setProductItem(productItem);
            formulationInputList.add(input);
        }
    }


    public List<FormulationInput> getFormulationInputList() {
        return formulationInputList;
    }

    public void setFormulationInputList(List<FormulationInput> formulationInputList) {
        this.formulationInputList = formulationInputList;
    }
}
