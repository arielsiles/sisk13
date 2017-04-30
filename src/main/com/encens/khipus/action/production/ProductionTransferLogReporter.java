package com.encens.khipus.action.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.ProductionTransferLog;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("productionTransferLogReporter")
@Scope(ScopeType.CONVERSATION)
public class ProductionTransferLogReporter {

    @Logger private Log log;

    @In private FacesMessages facesMessages;

    @In("extendedGenericService")
    private GenericService genericService;

    private Long id;
    private ProductionTransferLog productionTransferLog;

    public void prepare() {
        try {
            this.productionTransferLog = null;
            this.productionTransferLog = genericService.findById(ProductionTransferLog.class, id);
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductionTransferLog getProductionTransferLog() {
        return productionTransferLog;
    }
}
