package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.FormulationState;
import com.encens.khipus.model.xproduction.XFormulation;
import com.encens.khipus.model.xproduction.XFormulationInput;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.xproduction.XFormulationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Name("xformulationAction")
@Scope(ScopeType.CONVERSATION)
public class XFormulationAction extends GenericAction<XFormulation> {

    @In
    private XFormulationService xformulationService;

    private List<XFormulationInput> formulationInputList = new ArrayList<XFormulationInput>();

    @Factory(value = "xformulation", scope = ScopeType.STATELESS)
    public XFormulation initProductionPlan() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(XFormulation instance) {
        String outCome = super.select(instance);
        setOp(OP_UPDATE);
        setFormulationInputList(getInstance().getFormulationInputList());
        return outCome;
    }

    @Override
    public String update() {

        xformulationService.updateFormulation(getInstance(), formulationInputList);
        return Outcome.SUCCESS;
    }

    public void removeInput(XFormulationInput formulationInput){

        xformulationService.removeInput(formulationInput);
        formulationInputList.remove(formulationInput);

    }

    public void approveFormulation(){
        if (!validateDefaultInput()) {
            return;
        }
        XFormulation formulation = getInstance();
        formulation.setState(FormulationState.APR);
        xformulationService.updateFormulation(formulation, formulationInputList);
    }

    /**
     * Exactamente un insumo debe quedar marcado como 'defecto': es el que define la
     * M.P. Usada de las ordenes que usan la formulacion. Sin ninguno el consumo de esas
     * ordenes se calcula en cero, y con varios se contaria mas de una vez.
     */
    private boolean validateDefaultInput() {
        int defaults = 0;
        for (XFormulationInput formulationInput : formulationInputList) {
            if (Boolean.TRUE.equals(formulationInput.getInputDefault())) {
                defaults++;
            }
        }

        if (defaults == 1) return true;

        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                defaults == 0 ? "XFormulation.error.defaultInputRequired"
                              : "XFormulation.error.defaultInputUnique");
        return false;
    }

    public void annulFormulation(){
        XFormulation formulation = getInstance();
        formulation.setState(FormulationState.ANL);
        xformulationService.updateFormulation(formulation, formulationInputList);
    }

    public void addIngredientItems(List<ProductItem> productItems) {

        for (ProductItem productItem : productItems) {
            if (!contains(productItem.getProductItemCode(), formulationInputList)){
                XFormulationInput input = new XFormulationInput();
                input.setProductItemCode(productItem.getProductItemCode());
                input.setProductItem(productItem);
                formulationInputList.add(input);
            }
        }
    }

    private boolean contains(String productItemCode, List<XFormulationInput> formulationInputList){

        Iterator<XFormulationInput> iterator = formulationInputList.iterator();
        boolean result = false;
        while (iterator.hasNext()){
            XFormulationInput item = iterator.next();
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

    public List<XFormulationInput> getFormulationInputList() {
        return formulationInputList;
    }

    public void setFormulationInputList(List<XFormulationInput> formulationInputList) {
        this.formulationInputList = formulationInputList;
    }
}
