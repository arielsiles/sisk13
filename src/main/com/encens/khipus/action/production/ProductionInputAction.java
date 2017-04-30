package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductionInput;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("productionInputAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionInputAction extends GenericAction<ProductionInput> {

    @In(value = "extendedGenericService")
    protected GenericService extendedGenericService;

    @Factory(value = "productionInput", scope = ScopeType.STATELESS)
    public ProductionInput initProductionInput() {
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
