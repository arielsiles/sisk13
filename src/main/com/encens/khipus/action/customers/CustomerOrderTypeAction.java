package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CustomerOrderType;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("customerOrderTypeAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerOrderTypeAction extends GenericAction<CustomerOrderType> {

    @Factory("customerOrderTypeEnumList")
    public CustomerOrderTypeEnum[] getCustomerOrderTypesEnum() {
        return CustomerOrderTypeEnum.values();
    }

}
