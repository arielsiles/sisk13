package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.Formulation;
import com.encens.khipus.model.production.FormulationInput;
import com.encens.khipus.model.production.FormulationState;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.production.FormulationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Name("formulationAction")
@Scope(ScopeType.CONVERSATION)
public class FormulationAction extends GenericAction<Formulation> {

    @In
    private FormulationService formulationService;

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

    @Override
    public String update() {

        formulationService.updateFormulation(getInstance(), formulationInputList);
        return Outcome.SUCCESS;
    }

    public void removeInput(FormulationInput formulationInput){

        formulationService.removeInput(formulationInput);
        formulationInputList.remove(formulationInput);

    }

    public void approveFormulation(){
        Formulation formulation = getInstance();
        formulation.setState(FormulationState.APR);
        formulationService.updateFormulation(formulation, formulationInputList);
    }

    public void annulFormulation(){
        Formulation formulation = getInstance();
        formulation.setState(FormulationState.ANL);
        formulationService.updateFormulation(formulation, formulationInputList);
    }

    public void addIngredientItems(List<ProductItem> productItems) {

        for (ProductItem productItem : productItems) {
            if (!contains(productItem.getProductItemCode(), formulationInputList)){
                FormulationInput input = new FormulationInput();
                input.setProductItemCode(productItem.getProductItemCode());
                input.setProductItem(productItem);
                formulationInputList.add(input);
            }
        }
    }

    private boolean contains(String productItemCode, List<FormulationInput> formulationInputList){

        Iterator<FormulationInput> iterator = formulationInputList.iterator();
        boolean result = false;
        while (iterator.hasNext()){
            FormulationInput item = iterator.next();
            if (item.getProductItemCode().equals(productItemCode)){
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean isPending(){
        return getInstance().getState().equals(FormulationState.PEN);
    }

    public boolean isApproved(){
        return getInstance().getState().equals(FormulationState.APR);
    }

    public boolean isAnnulled(){
        return getInstance().getState().equals(FormulationState.ANL);
    }

    public List<FormulationInput> getFormulationInputList() {
        return formulationInputList;
    }

    public void setFormulationInputList(List<FormulationInput> formulationInputList) {
        this.formulationInputList = formulationInputList;
    }
}
