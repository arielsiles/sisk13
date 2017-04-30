package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.DiscountPolicy;
import com.encens.khipus.model.customers.DiscountPolicyMeasurementType;
import com.encens.khipus.model.customers.DiscountPolicyTargetType;
import com.encens.khipus.model.customers.DiscountPolicyType;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;

/**
 * Actions for Discount Policy
 *
 * @author:
 */

@Name("discountPolicyAction")
@Scope(ScopeType.CONVERSATION)
public class DiscountPolicyAction extends GenericAction<DiscountPolicy> {

    @In(required = false)
    private User currentUser;

    @Factory(value = "discountPolicy", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('DISCOUNTPOLICY','VIEW')}")
    public DiscountPolicy initDiscountPolicy() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('DISCOUNTPOLICY','VIEW')}")
    public String select(DiscountPolicy discountPolicy) {
        String result = super.select(discountPolicy);
        if (com.encens.khipus.framework.action.Outcome.SUCCESS.equals(result)) {
            getInstance().setTarget(getInstance().getDiscountPolicyType().getTarget());
        }
        return result;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Factory("discountPolicyTargetType")
    public DiscountPolicyTargetType[] getTargetType() {
        return DiscountPolicyTargetType.values();
    }

    public void validatePercentage(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (context == null) {
            throw new NullPointerException("FacesContext is null");
        }
        if (component == null) {
            throw new NullPointerException("uiComponent is null");
        }
        if (value == null) {
            return;
        }

        UIComponent foreignComp = JSFUtil.findComponent(component, "discountPolicyType");
        if (foreignComp == null) {
            throw new FacesException("Unable to find component");
        }
        HtmlSelectOneMenu input = (HtmlSelectOneMenu) foreignComp;
        DiscountPolicyMeasurementType discountPolicyType = ((DiscountPolicyType) input.getValue()).getMeasurement();

        if (discountPolicyType.equals(DiscountPolicyMeasurementType.PERCENTAGE)) {
            double percentage = ((BigDecimal) value).doubleValue();
            if (percentage > 100.0 || percentage < 0.0) {
                throw new ValidatorException(null);
            }
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISCOUNTPOLICY','CREATE')}")
    public String create() {
        getInstance().setUser(currentUser);
        System.out.println("getInstance().getAmount()=" + getInstance().getAmount());
        return super.create();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISCOUNTPOLICY','UPDATE')}")
    public String update() {
        getInstance().setUser(currentUser);
        return super.update();
    }
}
