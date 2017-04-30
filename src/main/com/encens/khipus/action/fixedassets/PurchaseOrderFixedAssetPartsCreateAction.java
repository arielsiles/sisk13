package com.encens.khipus.action.fixedassets;

import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.PurchaseOrderFixedAssetPart;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.3
 */
@Name("purchaseOrderFixedAssetPartsCreateAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseOrderFixedAssetPartsCreateAction implements Serializable {

    private PurchaseOrderFixedAssetPart instance = new PurchaseOrderFixedAssetPart();

    private List<PurchaseOrderFixedAssetPart> instances = new ArrayList<PurchaseOrderFixedAssetPart>();

    @In
    private FacesMessages facesMessages;

    @In
    private FixedAssetPurchaseOrderAction fixedAssetPurchaseOrderAction;

    @Create
    public void initializeAction() {

    }

    public void addInstance() {
        if (com.encens.khipus.framework.action.Outcome.SUCCESS.equals(validateRequiredElements(getDetailRequiredElements()))) {
            PurchaseOrderFixedAssetPart purchaseOrderFixedAssetPart = new PurchaseOrderFixedAssetPart();
            purchaseOrderFixedAssetPart.setFixedAsset(instance.getFixedAsset());
            purchaseOrderFixedAssetPart.setDescription(instance.getDescription());
            purchaseOrderFixedAssetPart.setMeasureUnit(instance.getMeasureUnit());
            purchaseOrderFixedAssetPart.setSerialNumber(instance.getSerialNumber());
            purchaseOrderFixedAssetPart.setUnitPrice(instance.getUnitPrice());
            purchaseOrderFixedAssetPart.setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
            instances.add(purchaseOrderFixedAssetPart);
            initializeInstance();
        }
    }

    public void removeInstance(PurchaseOrderFixedAssetPart instance) {
        instances.remove(instance);
    }


    public void putFixedAsset(FixedAsset fixedAsset) {
        instance.setFixedAsset(fixedAsset);
    }

    public void cleanFixedAsset() {
        instance.setFixedAsset(null);
    }


    public PurchaseOrderFixedAssetPart getInstance() {
        return instance;
    }

    public void setInstance(PurchaseOrderFixedAssetPart instance) {
        this.instance = instance;
    }

    public List<PurchaseOrderFixedAssetPart> getInstances() {
        return instances;
    }

    public void setInstances(List<PurchaseOrderFixedAssetPart> instances) {
        this.instances = instances;
    }

    public Integer getInstancesRowCounter() {
        return instances.size() + 1;
    }

    private void initializeInstance() {
        instance = new PurchaseOrderFixedAssetPart();
    }

    private String validateRequiredElements(List<ValidateElement> elements) {
        String validationOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS;
        for (ValidateElement validateElement : elements) {
            if (validateElement.isValidateNotNull() && isNullValue(validateElement.getValue(), validateElement.getKey())) {
                validationOutcome = com.encens.khipus.framework.action.Outcome.REDISPLAY;
            }

            if (validateElement.isValidateRange() && isInvalidRange(validateElement)) {
                validationOutcome = com.encens.khipus.framework.action.Outcome.REDISPLAY;
            }
        }

        return validationOutcome;
    }


    private boolean isNullValue(Object value, String message) {
        if (value instanceof String) {
            if (ValidatorUtil.isBlankOrNull((String) value)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Common.required", message);
                return true;
            }

            return false;
        }

        if (null == value) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.required", message);
            return true;
        }

        return false;
    }

    private boolean isInvalidRange(ValidateElement validateElement) {
        Object value = validateElement.getValue();
        String message = validateElement.getKey();
        if (value instanceof Number) {
            BigDecimal bigDecimalValue = BigDecimalUtil.toSimpleBigDecimal(value);
            if (validateElement.getMoreThan() != null && validateElement.getLessThan() != null
                    && (bigDecimalValue.compareTo(validateElement.getMoreThan()) < 0 || bigDecimalValue.compareTo(validateElement.getLessThan()) > 0)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrderFixedAssetPart.error.range", message, validateElement.getMoreThan(), validateElement.getLessThan());
                return true;
            } else if (validateElement.getMoreThan() != null && validateElement.getLessThan() == null
                    && bigDecimalValue.compareTo(validateElement.getMoreThan()) < 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrderFixedAssetPart.error.moreThan", message, validateElement.getMoreThan());
                return true;
            } else if (validateElement.getMoreThan() == null && validateElement.getLessThan() != null
                    && bigDecimalValue.compareTo(validateElement.getLessThan()) > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PurchaseOrderFixedAssetPart.error.lessThan", message, validateElement.getLessThan());
                return true;
            }
        }

        return false;
    }

    private ValidateElement getValidateInstance(Object value, String key) {
        return new ValidateElement(value, key);
    }

    private List<ValidateElement> getDetailRequiredElements() {
        List<ValidateElement> elements = new ArrayList<ValidateElement>();
        elements.add(getValidateInstance(instance.getFixedAsset(), MessageUtils.getMessage("PurchaseOrderFixedAssetPart.fixedAsset")));
        elements.add(getValidateInstance(instance.getDescription(), MessageUtils.getMessage("PurchaseOrderFixedAssetPart.description")));
        elements.add(getValidateInstance(instance.getMeasureUnit(), MessageUtils.getMessage("PurchaseOrderFixedAssetPart.measureUnit")));
        elements.add(getValidateInstance(instance.getUnitPrice(), MessageUtils.getMessage("PurchaseOrderFixedAssetPart.unitPrice")).validateRange(BigDecimal.ONE, null));
        return elements;
    }

    private final class ValidateElement {
        private Object value;
        private String key;
        private boolean validateNotNull = true;
        private BigDecimal moreThan = null;
        private BigDecimal lessThan = null;

        private ValidateElement(Object value, String key) {
            this.value = value;
            this.key = key;
        }


        public Object getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }

        public boolean isValidateNotNull() {
            return validateNotNull;
        }

        public BigDecimal getMoreThan() {
            return moreThan;
        }

        public void setMoreThan(BigDecimal moreThan) {
            this.moreThan = moreThan;
        }

        public BigDecimal getLessThan() {
            return lessThan;
        }

        public void setLessThan(BigDecimal lessThan) {
            this.lessThan = lessThan;
        }

        public boolean isValidateRange() {
            return getMoreThan() != null || getLessThan() != null;
        }

        public ValidateElement validateRange(BigDecimal moreThan, BigDecimal lessThan) {
            setMoreThan(moreThan);
            setLessThan(lessThan);
            return this;
        }

    }
}