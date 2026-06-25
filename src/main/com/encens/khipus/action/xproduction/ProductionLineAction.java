package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.service.warehouse.ProductItemService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.NoResultException;

@Name("productionLineAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionLineAction extends GenericAction<ProductionLine> {

    @In(create = true)
    private ProductItemService productItemService;

    /* Articulos seleccionados (transient, solo para mostrar codigo + nombre en la vista). */
    private ProductItem mpPrincipalItem;
    private ProductItem ptAItem;
    private ProductItem ptBItem;
    private ProductItem diluyBentItem;
    private ProductItem diluyCaolinItem;
    private ProductItem reprocFinalItem;
    private ProductItem ptPrincipalItem;

    @Factory(value = "productionLine", scope = ScopeType.STATELESS)
    public ProductionLine initXProductionLine() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(ProductionLine instance) {
        String outcome = super.select(instance);
        if (Outcome.SUCCESS.equals(outcome)) {
            loadProductItems();
        }
        return outcome;
    }

    /** Resuelve los articulos a partir de los codigos almacenados para mostrar codigo + nombre al editar. */
    private void loadProductItems() {
        ProductionLine line = getInstance();
        mpPrincipalItem = findProductItem(line.getCodArtMpPrincipal());
        ptAItem = findProductItem(line.getCodArtPtA());
        ptBItem = findProductItem(line.getCodArtPtB());
        diluyBentItem = findProductItem(line.getCodArtDiluyBent());
        diluyCaolinItem = findProductItem(line.getCodArtDiluyCaolin());
        reprocFinalItem = findProductItem(line.getCodArtReprocFinal());
        ptPrincipalItem = findProductItem(line.getCodArtPtPrincipal());
    }

    private ProductItem findProductItem(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return productItemService.findProductItemByCode(code);
        } catch (NoResultException e) {
            return null;
        }
    }

    /* ULEXITA: MP principal */
    public ProductItem getMpPrincipalItem() {
        return mpPrincipalItem;
    }

    public void setMpPrincipalItem(ProductItem mpPrincipalItem) {
        this.mpPrincipalItem = mpPrincipalItem;
        getInstance().setCodArtMpPrincipal(mpPrincipalItem == null ? null : mpPrincipalItem.getProductItemCode());
    }

    public void assignMpPrincipal(ProductItem productItem) {
        setMpPrincipalItem(productItem);
    }

    public void clearMpPrincipal() {
        setMpPrincipalItem(null);
    }

    /* ULEXITA: PT A */
    public ProductItem getPtAItem() {
        return ptAItem;
    }

    public void setPtAItem(ProductItem ptAItem) {
        this.ptAItem = ptAItem;
        getInstance().setCodArtPtA(ptAItem == null ? null : ptAItem.getProductItemCode());
    }

    public void assignPtA(ProductItem productItem) {
        setPtAItem(productItem);
    }

    public void clearPtA() {
        setPtAItem(null);
    }

    /* ULEXITA: PT B */
    public ProductItem getPtBItem() {
        return ptBItem;
    }

    public void setPtBItem(ProductItem ptBItem) {
        this.ptBItem = ptBItem;
        getInstance().setCodArtPtB(ptBItem == null ? null : ptBItem.getProductItemCode());
    }

    public void assignPtB(ProductItem productItem) {
        setPtBItem(productItem);
    }

    public void clearPtB() {
        setPtBItem(null);
    }

    /* ULEXITA: diluyente bentonita */
    public ProductItem getDiluyBentItem() {
        return diluyBentItem;
    }

    public void setDiluyBentItem(ProductItem diluyBentItem) {
        this.diluyBentItem = diluyBentItem;
        getInstance().setCodArtDiluyBent(diluyBentItem == null ? null : diluyBentItem.getProductItemCode());
    }

    public void assignDiluyBent(ProductItem productItem) {
        setDiluyBentItem(productItem);
    }

    public void clearDiluyBent() {
        setDiluyBentItem(null);
    }

    /* ULEXITA: diluyente caolin */
    public ProductItem getDiluyCaolinItem() {
        return diluyCaolinItem;
    }

    public void setDiluyCaolinItem(ProductItem diluyCaolinItem) {
        this.diluyCaolinItem = diluyCaolinItem;
        getInstance().setCodArtDiluyCaolin(diluyCaolinItem == null ? null : diluyCaolinItem.getProductItemCode());
    }

    public void assignDiluyCaolin(ProductItem productItem) {
        setDiluyCaolinItem(productItem);
    }

    public void clearDiluyCaolin() {
        setDiluyCaolinItem(null);
    }

    /* ULEXITA: articulo destino del Reproceso final (ej. 'Ulexita Procesada') */
    public ProductItem getReprocFinalItem() {
        return reprocFinalItem;
    }

    public void setReprocFinalItem(ProductItem reprocFinalItem) {
        this.reprocFinalItem = reprocFinalItem;
        getInstance().setCodArtReprocFinal(reprocFinalItem == null ? null : reprocFinalItem.getProductItemCode());
    }

    public void assignReprocFinal(ProductItem productItem) {
        setReprocFinalItem(productItem);
    }

    public void clearReprocFinal() {
        setReprocFinalItem(null);
    }

    /* BARITINA: PT principal */
    public ProductItem getPtPrincipalItem() {
        return ptPrincipalItem;
    }

    public void setPtPrincipalItem(ProductItem ptPrincipalItem) {
        this.ptPrincipalItem = ptPrincipalItem;
        getInstance().setCodArtPtPrincipal(ptPrincipalItem == null ? null : ptPrincipalItem.getProductItemCode());
    }

    public void assignPtPrincipal(ProductItem productItem) {
        setPtPrincipalItem(productItem);
    }

    public void clearPtPrincipal() {
        setPtPrincipalItem(null);
    }

}
