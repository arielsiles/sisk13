package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.ProductCompositionException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductComposition;
import com.encens.khipus.model.production.ProductionIngredient;
import com.encens.khipus.service.production.EvaluatorMathematicalExpressionsService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import javax.faces.event.ActionEvent;

import static com.encens.khipus.exception.production.ProductCompositionException.NO_FOUND_VARIABLE;
import static com.encens.khipus.exception.production.ProductCompositionException.TOPOLOGICAL_SORTING;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.WARN;

@Name("productCompositionAction")
@Scope(ScopeType.CONVERSATION)
public class ProductCompositionAction extends GenericAction<ProductComposition> {

    private ProcessedProduct processedProduct;

    private MetaProduct metaProduct;

    private ProductComposition backup;

    @In
    private EvaluatorMathematicalExpressionsService evaluatorMathematicalExpressionsService;
    @In
    private GenericService extendedGenericService;
    @In("#{messages['ProductComposition.defaultFormulaMathematica']}")
    private String defaultMathematicalFormula;

    @Override
    protected GenericService getService() {
        return extendedGenericService;
    }

    @Factory(value = "productComposition", scope = ScopeType.STATELESS)
    public ProductComposition initProductComposition() {
        return getInstance();
    }

    @Override
    protected Object getDisplayPropertyValue() {
        return processedProduct.getName();
    }

    private void FixProcessedProduct() {
        ProductComposition p = getInstance();
        p.setProcessedProduct(processedProduct);
    }

    @Begin(ifOutcome = "Success", join = true)
    public String startCreateNew() {
        return "Success";
    }

    public void processedProductSelected(ActionEvent e) {
        FixProcessedProduct();
    }

    public void selectProcessedProduct(ProcessedProduct processedProduct) {
        this.processedProduct = processedProduct;
        FixProcessedProduct();
    }

    public void selectMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
        if (findIngredient() != null) {
            facesMessages.addFromResourceBundle(WARN, "ProductComposition.error.metaProductCurrentlyAdded", metaProduct.getName());
            return;
        }

        registerIngredient(defaultMathematicalFormula);
    }

    public boolean mathematicalFormulaActionListener(ActionEvent e) {

        try {
            ProductComposition productComposition = getInstance();
            if (productComposition.getProducingAmount() == null || productComposition.getContainerWeight() == null || productComposition.getSupposedAmount() == null) {
                return false;
            }

            for (ProductionIngredient pi : productComposition.getProductionIngredientList()) {
                if (pi.getMathematicalFormula() == null) {
                    return false;
                }
            }

            evaluatorMathematicalExpressionsService.executeMathematicalFormulas(productComposition);
            return true;
        } catch (Exception ex1) {
            Throwable throwable = ex1;
            do {
                if (throwable instanceof ProductCompositionException) {
                    ProductCompositionException pce = (ProductCompositionException) throwable;

                    if (pce.getCode() == NO_FOUND_VARIABLE) {
                        facesMessages.addFromResourceBundle(WARN, "ProductComposition.warning.noFoundVariable", pce.getData());
                        return false;
                    }

                    if (pce.getCode() != TOPOLOGICAL_SORTING) {
                        throw new RuntimeException(throwable);
                    }

                    if (pce.isCyclic()) {
                        facesMessages.addFromResourceBundle(WARN, "ProductComposition.warning.cyclicFormulaDetected");
                    }
                    if (pce.getData() != null) {
                        facesMessages.addFromResourceBundle(WARN, "ProductComposition.warning.wrongFormula", pce.getData());
                    }
                    return false;
                }
            } while ((throwable = throwable.getCause()) != null);

            log.error("Exception ", ex1);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");

            return false;
        }
    }

    private void registerIngredient(String mathematicalFormula) {
        ProductionIngredient ingredient = new ProductionIngredient();
        ingredient.setMathematicalFormula(mathematicalFormula);
        ingredient.setMetaProduct(metaProduct);
        ingredient.setProductComposition(getInstance());

        ProductComposition product = getInstance();
        product.getProductionIngredientList().add(ingredient);
    }

    private ProductionIngredient findIngredient() {
        for (ProductionIngredient pi : getInstance().getProductionIngredientList()) {
            if (pi.getMetaProduct().getId().equals(metaProduct.getId())) {
                return pi;
            }
        }
        return null;
    }

    public String removeInputProduction(ProductionIngredient input) {
        setMetaProduct(input.getMetaProduct());
        ProductionIngredient ingredient = findIngredient();
        if (ingredient != null) {
            ingredient.setMetaProduct(null);
            ingredient.setProductComposition(null);
            getInstance().getProductionIngredientList().remove(ingredient);
        }
        return null;
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String create() {
        if (mathematicalFormulaActionListener(null) == false) {
            return Outcome.REDISPLAY;
        }

        return super.create();
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String update() {
        if (mathematicalFormulaActionListener(null) == false) {
            return Outcome.REDISPLAY;
        }

        return super.update();
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String delete() {
        if (mathematicalFormulaActionListener(null) == false) {
            return Outcome.REDISPLAY;
        }

        setInstance(backup);
        return super.update();
    }

    @Override
    public ProductComposition createInstance() {
        ProductComposition pc = super.createInstance();
        if (pc != null) {
            pc.setActive(true);
        }
        return pc;
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(ProductComposition productComposition) {
        try {
            setOp(OP_UPDATE);
            inactivateAsBackup(productComposition);
            setProcessedProduct(productComposition.getProcessedProduct());
            FixProcessedProduct();

            getInstance().setProducingAmount(productComposition.getProducingAmount());
            getInstance().setContainerWeight(productComposition.getContainerWeight());
            getInstance().setSupposedAmount(productComposition.getSupposedAmount());
            getInstance().setName(productComposition.getName());

            for (ProductionIngredient pi : backup.getProductionIngredientList()) {
                setMetaProduct(pi.getMetaProduct());
                registerIngredient(pi.getMathematicalFormula());
            }

            if (mathematicalFormulaActionListener(null) == true)
                return Outcome.SUCCESS;
            else
                return Outcome.REDISPLAY;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    private void inactivateAsBackup(ProductComposition productComposition) throws EntryNotFoundException {
        backup = getService().findById(productComposition.getClass(), productComposition.getId());
        backup.setActive(false);
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public ProcessedProduct getProcessedProduct() {
        return processedProduct;
    }

    public void setProcessedProduct(ProcessedProduct processedProduct) {
        this.processedProduct = processedProduct;
    }

    public double getTotalWeight() {
        double total = 0.0;

        for (ProductionIngredient ingredient : getInstance().getProductionIngredientList()) {
            //total += ingredient.getAmount();
            if (ingredient.getMetaProduct().getProductItem().getUsageMeasureCode().equals("GR"))
                total = total + ingredient.getAmount() / 1000.0;
            else
                total = total + ingredient.getAmount();
        }
        return total;
    }
}