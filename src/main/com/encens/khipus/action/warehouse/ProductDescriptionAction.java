package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.ProductDescription;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

/**
 * CRUD del catalogo de Descripciones Tecnicas de Producto.
 *
 * Ciclo de estados: BORRADOR -> APROBADO -> INACTIVO (sin vuelta atras).
 * APROBADO/INACTIVO son inmutables: actualizaciones/eliminaciones bloqueadas.
 */
@Name("productDescriptionAction")
@Scope(ScopeType.CONVERSATION)
public class ProductDescriptionAction extends GenericAction<ProductDescription> {

    @Factory(value = "productDescription", scope = ScopeType.STATELESS)
    public ProductDescription initProductDescription() {
        return getInstance();
    }

    @Factory(value = "catalogApprovalStateList")
    public CatalogApprovalState[] getStateList() {
        return CatalogApprovalState.values();
    }

    /**
     * Permite al popup de seleccion de producto setear el FK desde el form.
     * Mantiene productItemCompanyNumber/productItemCode sincronizados.
     */
    public void assignProductItem(ProductItem productItem) {
        getInstance().setProductItem(productItem);
    }

    @Override
    public String create() {
        ProductDescription p = getInstance();
        if (p.getState() == null) {
            p.setState(CatalogApprovalState.BORRADOR);
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    public String update() {
        ProductDescription p = getInstance();
        if (!p.isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProductDescription.error.notEditable");
            return Outcome.REDISPLAY;
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    public String delete() {
        if (!getInstance().isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProductDescription.error.notDeletable");
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /**
     * Transicion BORRADOR -> APROBADO. Persiste el cambio inmediatamente.
     */
    public String approve() {
        ProductDescription p = getInstance();
        if (p.getState() != CatalogApprovalState.BORRADOR) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProductDescription.error.cannotApprove");
            return Outcome.REDISPLAY;
        }
        if (!validateNotEmpty()) {
            return Outcome.REDISPLAY;
        }
        p.setState(CatalogApprovalState.APROBADO);
        return super.update();
    }

    /**
     * Transicion APROBADO -> INACTIVO. Persiste el cambio inmediatamente.
     */
    public String inactivate() {
        ProductDescription p = getInstance();
        if (p.getState() != CatalogApprovalState.APROBADO) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProductDescription.error.cannotInactivate");
            return Outcome.REDISPLAY;
        }
        p.setState(CatalogApprovalState.INACTIVO);
        return super.update();
    }

    private boolean validateNotEmpty() {
        ProductDescription p = getInstance();
        if (p.getProductItem() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProductDescription.error.productItemRequired");
            return false;
        }
        if (p.getDescription() == null || p.getDescription().trim().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProductDescription.error.descriptionRequired");
            return false;
        }
        return true;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "shortDescription";
    }
}
