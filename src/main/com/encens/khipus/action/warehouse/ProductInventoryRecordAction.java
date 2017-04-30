package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.ProductInventoryRecord;
import com.encens.khipus.model.warehouse.ProductInventoryRecordType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 3.0
 */
@Name("productInventoryRecordAction")
@Scope(ScopeType.CONVERSATION)
public class ProductInventoryRecordAction extends GenericAction<ProductInventoryRecord> {


    @Factory(value = "productInventoryRecord", scope = ScopeType.STATELESS)
    public ProductInventoryRecord initProductInventoryRecord() {
        return getInstance();
    }



    @Factory(value = "productInventoryRecordTypes", scope = ScopeType.STATELESS)
    public ProductInventoryRecordType[] initProductInventoryRecordTypes() {
        return ProductInventoryRecordType.values();
    }

}
