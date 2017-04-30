package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.2
 */
@Name("provideAction")
@Scope(ScopeType.CONVERSATION)
public class ProvideAction extends GenericAction<Provide> {

    private ProductItem productItem = null;

    @In
    private ProviderAction providerAction;
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;
    @In(create = true)
    private EntityQuery countProductItemByProviderQuery;

    @Factory(value = "provide", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
    public Provide initProvide() {
        return getInstance();
    }

    @Begin(nested = true, ifOutcome = Outcome.SUCCESS)
    public String addProvideElement() {
        getInstance().setProviderCode(providerAction.getProvider().getId().getProviderCode());
        return Outcome.SUCCESS;
    }

    @Override
    @Begin(nested = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
    public String select(Provide instance) {
        String outcome = super.select(instance);

        if (Outcome.SUCCESS.equals(outcome)) {
            productItem = findProductItem(getInstance().getProductItem().getId());
        }
        return outcome;
    }

    @End(beforeRedirect = true)
    @Override
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','CREATE')}")
    public String create() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        getInstance().setProductItem(productItem);
        getInstance().setGroupMeasureUnit(getGroupMeasureUnit());

        String outcome = super.create();
        if (null != outcome && outcome.equals(Outcome.SUCCESS)) {
            ProductItemByProviderHistory productItemByProviderHistory;
            productItemByProviderHistory = new ProductItemByProviderHistory();
            productItemByProviderHistory.setDate(new Date());
            productItemByProviderHistory.setProvide(getInstance());
            productItemByProviderHistory.setUnitCost(getInstance().getGroupAmount());
            try {
                getService().create(productItemByProviderHistory);
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
                return Outcome.REDISPLAY;
            }
            return Outcome.SUCCESS;
        } else {
            return outcome;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','CREATE')}")
    public void createAndNew() {
        if (Outcome.SUCCESS.equals(validateInputFields())) {
            getInstance().setProductItem(productItem);
            getInstance().setGroupMeasureUnit(getGroupMeasureUnit());
            try {
                getService().create(getInstance());
                addCreatedMessage();
                ProductItemByProviderHistory productItemByProviderHistory;
                productItemByProviderHistory = new ProductItemByProviderHistory();
                productItemByProviderHistory.setDate(new Date());
                productItemByProviderHistory.setProvide(getInstance());
                productItemByProviderHistory.setUnitCost(getInstance().getGroupAmount());
                try {
                    getService().create(productItemByProviderHistory);
                } catch (EntryDuplicatedException e) {
                    addDuplicatedMessage();
                }
                createInstance();
            } catch (EntryDuplicatedException e) {
                addDuplicatedMessage();
            }

            setProductItem(null);
            getInstance().setProviderCode(providerAction.getProvider().getId().getProviderCode());
        }
    }

    @End(beforeRedirect = true)
    @Override
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','UPDATE')}")
    public String update() {
        String validationOutcome = validateInputFields();
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return validationOutcome;
        }

        getInstance().setProductItem(productItem);
        getInstance().setGroupMeasureUnit(getGroupMeasureUnit());
        Provide eventProvide = eventEm.find(Provide.class, getInstance().getId());
        BigDecimal dbGroupAmount = BigDecimalUtil.toBigDecimal(eventProvide.getGroupAmount().doubleValue());
        String outcome = super.update();
        if (outcome.equals(Outcome.SUCCESS)) {
            if (getInstance().getGroupAmount().compareTo(dbGroupAmount) != 0) {
                ProductItemByProviderHistory productItemByProviderHistory;
                productItemByProviderHistory = new ProductItemByProviderHistory();
                productItemByProviderHistory.setDate(new Date());
                productItemByProviderHistory.setProvide(getInstance());
                productItemByProviderHistory.setUnitCost(getInstance().getGroupAmount());
                try {
                    getService().create(productItemByProviderHistory);
                } catch (EntryDuplicatedException e) {
                    addDuplicatedMessage();
                    return Outcome.REDISPLAY;
                }
            }
            return outcome;
        } else {
            return outcome;
        }
    }

    @End(beforeRedirect = true)
    @Override
    @Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','DELETE')}")
    public String delete() {
        return super.delete();
    }

    @End(beforeRedirect = true)
    @Override
    public String cancel() {
        return super.cancel();
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = findProductItem(productItem.getId());
    }

    public void cleanProductItem() {
        this.productItem = null;
        getInstance().setProductItem(null);
        getInstance().setGroupMeasureUnit(null);
    }

    public String validateInputFields() {
        String validationOutcome = Outcome.SUCCESS;

        if (null == this.productItem) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", MessageUtils.getMessage("Provide.productItem"));

            validationOutcome = Outcome.REDISPLAY;
        }

        Long countProductItemByProvider = (Long) countProductItemByProviderQuery.getSingleResult();

        if (null != this.productItem && countProductItemByProvider != null && countProductItemByProvider > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Provide.error.duplicated",
                    this.productItem.getFullName(),
                    providerAction.getProvider().getFullName()
            );

            validationOutcome = Outcome.REDISPLAY;
        }

        return validationOutcome;
    }

    @Override
    protected String getDisplayNameMessage() {
        if (null != getInstance().getProductItem()) {
            return getInstance().getProductItem().getName();
        }

        return super.getDisplayNameMessage();
    }

    public boolean isProductItemSelect() {
        return null != this.productItem;
    }

    public MeasureUnit getGroupMeasureUnit() {
        return isProductItemSelect() ? null != productItem.getGroupMeasureUnit() ? productItem.getGroupMeasureUnit() : productItem.getUsageMeasureUnit() : null;
    }


    public boolean isGroupMeasureUnitEnabled() {
        return null != this.productItem && null != productItem.getGroupMeasureUnit();
    }

    private ProductItem findProductItem(ProductItemPK id) {
        try {
            return getService().findById(ProductItem.class, id, true);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }
}
