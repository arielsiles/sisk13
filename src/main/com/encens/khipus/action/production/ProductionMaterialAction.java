package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductionMaterial;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("productionMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionMaterialAction extends GenericAction<ProductionMaterial> {

    @In(value = "extendedGenericService")
    private GenericService extendedGenericService;

    @Factory(value = "productionMaterial", scope = ScopeType.STATELESS)
    public ProductionMaterial initProductionMaterial() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    protected GenericService getService() {
        return extendedGenericService;
    }
}
