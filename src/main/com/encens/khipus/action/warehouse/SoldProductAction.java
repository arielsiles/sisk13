package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.SoldProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.0
 */
@Name("soldProductAction")
@Scope(ScopeType.CONVERSATION)
public class SoldProductAction extends GenericAction<SoldProduct> {


    private SoldProduct soldProduct;

    public SoldProduct getSoldProduct() {
        return soldProduct;
    }

    public void setSoldProduct(SoldProduct soldProduct) {
        this.soldProduct = soldProduct;
    }
}
