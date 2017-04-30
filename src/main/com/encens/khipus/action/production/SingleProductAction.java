package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.SingleProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("singleProductAction")
@Scope(ScopeType.CONVERSATION)
public class SingleProductAction extends GenericAction<SingleProduct> {

    //TODO change the name initContinent
    @Factory(value = "singleProductDelete", scope = ScopeType.STATELESS)
    public SingleProduct initSingleProduct() {
        return getInstance();
    }


}
