package com.encens.khipus.action.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductionOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("productionOrderReporter")
@Scope(ScopeType.CONVERSATION)
public class ProductionOrderReporter {

    @Logger
    private Log log;

    @In
    private FacesMessages facesMessages;

    @In("extendedGenericService")
    private GenericService genericService;

    private Long id;
    private ProductionOrder productionOrder;

    public void prepare() {
        try {
            log.info("preparing");
            this.productionOrder = genericService.findById(ProductionOrder.class, id);
        } catch (Exception ex) {
            log.error("Exception", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }
}
