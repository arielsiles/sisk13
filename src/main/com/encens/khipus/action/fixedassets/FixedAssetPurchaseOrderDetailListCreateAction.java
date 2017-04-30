package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.*;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.TrademarkSynchronizeService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.fixedassets.FixedAssetAmountUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the logic to add or remove the <code>FixedAssetPurchaseOrderDetail</code> objects, the fixed asset
 * purchase order details are added one by one and are stored in a <code>List</code> object they are used to show
 * the selected elements in the user view.
 *
 * @author
 * @version 2.23
 */
@Name("fixedAssetPurchaseOrderDetailListCreateAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetPurchaseOrderDetailListCreateAction implements Serializable {

    private FixedAssetPurchaseOrderDetail instance = new FixedAssetPurchaseOrderDetail();

    private List<FixedAssetPurchaseOrderDetail> instances = new ArrayList<FixedAssetPurchaseOrderDetail>();

    private PurchaseOrderDetailPart partInstance = new PurchaseOrderDetailPart();

    private FixedAssetGroup fixedAssetGroup;

    @Logger
    private Log log;

    @In
    private FacesMessages facesMessages;

    @In
    private FixedAssetPurchaseOrderAction fixedAssetPurchaseOrderAction;

    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private TrademarkSynchronizeService trademarkSynchronizeService;

    @Create
    public void initializeAction() {
        putDefaultExchangeRates(instance);
    }

    public void addInstance() {
        Trademark syncTrademark = trademarkSynchronizeService.synchronizeTrademark(
                instance.getTrademarkEntity(),
                instance.getTrademarkName());
        instance.setTrademarkEntity(syncTrademark);
        if (null != syncTrademark) {
            instance.setTrademark(syncTrademark.getName());
        } else {
            instance.setTrademark(null);
        }

        Model syncModel = trademarkSynchronizeService.synchronizeModel(
                instance.getModelEntity(),
                instance.getModelName());
        instance.setModelEntity(syncModel);
        if (null != syncModel) {
            instance.setModel(syncModel.getName());
        } else {
            instance.setModel(null);
        }
        if (Outcome.SUCCESS.equals(validateRequiredElements(getDetailRequiredElements()))
                && Outcome.SUCCESS.equals(validatePartAmounts())
                && Outcome.SUCCESS.equals(validateSubGroupPartsRequired())) {

            FixedAssetPurchaseOrderDetail fixedAssetPurchaseOrderDetail = new FixedAssetPurchaseOrderDetail();
            fixedAssetPurchaseOrderDetail.setBsSusRate(instance.getBsSusRate());
            fixedAssetPurchaseOrderDetail.setBsUfvRate(instance.getBsUfvRate());

            fixedAssetPurchaseOrderDetail.setFixedAssetSubGroup(instance.getFixedAssetSubGroup());
            fixedAssetPurchaseOrderDetail.setBsUnitPriceValue(instance.getBsUnitPriceValue());
            fixedAssetPurchaseOrderDetail.setSusUnitPriceValue(instance.getSusUnitPriceValue());
            fixedAssetPurchaseOrderDetail.setUfvUnitPriceValue(instance.getUfvUnitPriceValue());
            fixedAssetPurchaseOrderDetail.setRequestedQuantity(instance.getRequestedQuantity());
            fixedAssetPurchaseOrderDetail.setRubbish(instance.getRubbish());
            fixedAssetPurchaseOrderDetail.setDetail(instance.getDetail());
            fixedAssetPurchaseOrderDetail.setMeasurement(instance.getMeasurement());
            fixedAssetPurchaseOrderDetail.setBsTotalAmount(instance.getBsTotalAmount());
            fixedAssetPurchaseOrderDetail.setSusTotalAmount(instance.getSusTotalAmount());
            fixedAssetPurchaseOrderDetail.setUfvTotalAmount(instance.getUfvTotalAmount());
            fixedAssetPurchaseOrderDetail.setMonthsGuaranty(instance.getMonthsGuaranty());

            fixedAssetPurchaseOrderDetail.setTotalDuration(instance.getTotalDuration());
            fixedAssetPurchaseOrderDetail.setUsageDuration(instance.getUsageDuration());
            fixedAssetPurchaseOrderDetail.setNetDuration(instance.getNetDuration());
            fixedAssetPurchaseOrderDetail.setNetDuration(instance.getNetDuration());

            fixedAssetPurchaseOrderDetail.setTrademarkEntity(instance.getTrademarkEntity());
            fixedAssetPurchaseOrderDetail.putTrademarkName();

            fixedAssetPurchaseOrderDetail.setModelEntity(instance.getModelEntity());
            fixedAssetPurchaseOrderDetail.putModelName();

            if (instance.getRubbish().compareTo(instance.getUfvUnitPriceValue()) == 1) {
                fixedAssetPurchaseOrderDetail.setRubbish(new BigDecimal("0.01"));
            }

            fixedAssetPurchaseOrderDetail.setPurchaseOrder(fixedAssetPurchaseOrderAction.getInstance());
            fixedAssetPurchaseOrderDetail.setOrderDetailPartList(instance.getOrderDetailPartList());
            instances.add(fixedAssetPurchaseOrderDetail);

            initializeInstance();
        }
    }

    public void removeInstance(FixedAssetPurchaseOrderDetail instance) {
        instances.remove(instance);
    }

    public void addPartInstance() {
        String validationOutcome = validateRequiredElements(getPartRequiredElements());

        if (Outcome.SUCCESS.equals(validationOutcome)) {
            PurchaseOrderDetailPart newPart = new PurchaseOrderDetailPart();
            newPart.setDescription(partInstance.getDescription());
            newPart.setMeasureUnit(partInstance.getMeasureUnit());
            newPart.setUnitPrice(partInstance.getUnitPrice());

            instance.getOrderDetailPartList().add(newPart);

            partInstance = new PurchaseOrderDetailPart();
        }
    }

    public void removePartInstance(PurchaseOrderDetailPart partItem) {
        instance.getOrderDetailPartList().remove(partItem);
    }

    public void settingUpUnitPriceBs() {
        if (null != instance.getBsUnitPriceValue()) {
            if (null != instance.getBsSusRate()) {
                instance.setSusUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToExchangeCurrency(instance.getBsUnitPriceValue(), instance.getBsSusRate())
                );
            }

            if (null != instance.getBsUfvRate()) {
                instance.setUfvUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToExchangeCurrency(instance.getBsUnitPriceValue(), instance.getBsUfvRate())
                );
            }

            settingUpTotalAmountValues();
        }
    }

    public void settingUpUnitPriceUSD() {
        if (null != instance.getSusUnitPriceValue()) {
            if (null != instance.getBsSusRate()) {
                instance.setBsUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToLocalCurrency(instance.getSusUnitPriceValue(), instance.getBsSusRate())
                );
            }

            if (null != instance.getBsUfvRate() && null != instance.getBsUnitPriceValue()) {
                instance.setUfvUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToExchangeCurrency(instance.getBsUnitPriceValue(), instance.getBsUfvRate())
                );
            }

            settingUpTotalAmountValues();
        }
    }

    public void settingUpUnitPriceUFV() {
        if (null != instance.getUfvUnitPriceValue()) {
            if (null != instance.getUfvUnitPriceValue()) {
                instance.setBsUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToLocalCurrency(instance.getUfvUnitPriceValue(), instance.getUfvUnitPriceValue())
                );
            }

            if (null != instance.getBsSusRate() && null != instance.getBsUnitPriceValue()) {
                instance.setSusUnitPriceValue(
                        FixedAssetAmountUtil.i.convertToExchangeCurrency(instance.getBsUnitPriceValue(), instance.getBsUfvRate())
                );
            }

            settingUpTotalAmountValues();
        }
    }

    public void settingUpTotalAmountValues() {
        if (null != instance.getRequestedQuantity()) {
            if (null != instance.getBsUnitPriceValue()) {
                instance.setBsTotalAmount(BigDecimalUtil.multiply(
                        BigDecimalUtil.toBigDecimal(instance.getRequestedQuantity()), instance.getBsUnitPriceValue())
                );
            }

            if (null != instance.getSusUnitPriceValue()) {
                instance.setSusTotalAmount(BigDecimalUtil.multiply(
                        BigDecimalUtil.toBigDecimal(instance.getRequestedQuantity()), instance.getSusUnitPriceValue())
                );
            }

            if (null != instance.getUfvUnitPriceValue()) {
                instance.setUfvTotalAmount(BigDecimalUtil.multiply(
                        BigDecimalUtil.toBigDecimal(instance.getRequestedQuantity()), instance.getUfvUnitPriceValue())
                );
            }
        }
    }

    public void putGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
        cleanSubGroup();
    }

    public void cleanGroup() {
        this.fixedAssetGroup = null;
        cleanSubGroup();
    }

    public void putSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        instance.setFixedAssetSubGroup(fixedAssetSubGroup);
        putDefaultValuesForSelectedSubGroup();
    }

    public void cleanSubGroup() {
        instance.setFixedAssetSubGroup(null);
    }

    public void putDefaultValuesForSelectedSubGroup() {
        if (null != instance.getFixedAssetSubGroup()) {
            updateDefaultValuesForSelectedSubGroup(instance.getFixedAssetSubGroup(), instance);
        }
    }

    public void updateDefaultValuesForSelectedSubGroup(FixedAssetSubGroup subGroup,
                                                       FixedAssetPurchaseOrderDetail instance) {
        BigDecimal subGroupRubbish = new BigDecimal("0.01");
        if (null != subGroup.getRubbish()) {
            subGroupRubbish = subGroup.getRubbish();
        }

        instance.setRubbish(subGroupRubbish);
        instance.setDetail(subGroup.getDetail());
        instance.setTotalDuration(subGroup.getDuration());
        instance.setUsageDuration(0);
        instance.setNetDuration(subGroup.getDuration());
    }

    public void updateTotalDuration() {
        if (getInstance().getTotalDuration() != null) {
            if (getInstance().getUsageDuration() != null) {
                getInstance().setNetDuration(getInstance().getTotalDuration() - getInstance().getUsageDuration());
            } else if (getInstance().getNetDuration() != null) {
                getInstance().setUsageDuration(getInstance().getTotalDuration() - getInstance().getNetDuration());
            }
        }
    }

    public void updateUsageDuration() {
        if (getInstance().getUsageDuration() != null) {
            if (getInstance().getTotalDuration() != null) {
                getInstance().setNetDuration(getInstance().getTotalDuration() - getInstance().getUsageDuration());
            } else if (getInstance().getNetDuration() != null) {
                getInstance().setTotalDuration(getInstance().getNetDuration() + getInstance().getUsageDuration());
            }
        }
    }

    public void updateNetDuration() {
        if (getInstance().getNetDuration() != null) {
            if (getInstance().getTotalDuration() != null) {
                getInstance().setUsageDuration(getInstance().getTotalDuration() - getInstance().getNetDuration());
            } else if (getInstance().getUsageDuration() != null) {
                getInstance().setTotalDuration(getInstance().getNetDuration() + getInstance().getUsageDuration());
            }
        }
    }

    public FixedAssetPurchaseOrderDetail getInstance() {
        return instance;
    }

    public PurchaseOrderDetailPart getPartInstance() {
        return partInstance;
    }

    public void setPartInstance(PurchaseOrderDetailPart partInstance) {
        this.partInstance = partInstance;
    }

    public void setInstance(FixedAssetPurchaseOrderDetail instance) {
        this.instance = instance;
    }

    public List<FixedAssetPurchaseOrderDetail> getInstances() {
        return instances;
    }

    public void setInstances(List<FixedAssetPurchaseOrderDetail> instances) {
        this.instances = instances;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
    }

    public Integer getDetailListRowCounter() {
        return instances.size() + 1;
    }

    public Integer getPartListRowCounter() {
        if (null == instance.getOrderDetailPartList()) {
            return 1;
        }

        return instance.getOrderDetailPartList().size() + 1;
    }

    private String validateSubGroupPartsRequired() {
        String validationOutcome = Outcome.SUCCESS;
        if (null != instance.getFixedAssetSubGroup()
                && instance.getFixedAssetSubGroup().getRequireParts()
                && instance.getOrderDetailPartList().isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FixedAssetPurchaseOrderDetail.error.requireParts");

            validationOutcome = Outcome.REDISPLAY;
        }
        return validationOutcome;
    }

    private String validatePartAmounts() {
        String validationOutcome = Outcome.SUCCESS;
        if (!instance.getOrderDetailPartList().isEmpty()) {
            BigDecimal totalUnitPrices = BigDecimal.ZERO;
            for (PurchaseOrderDetailPart part : instance.getOrderDetailPartList()) {
                totalUnitPrices = BigDecimalUtil.sum(totalUnitPrices, part.getUnitPrice());
            }

            if (null == instance.getBsUnitPriceValue()) {
                validationOutcome = Outcome.REDISPLAY;
            } else {
                if (instance.getBsUnitPriceValue().compareTo(totalUnitPrices) != 0) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "PurchaseOrderDetailPart.error.totalUnitPricesDistinctPurchaseOrderDetailTotal",
                            instance.getBsUnitPriceValue());

                    validationOutcome = Outcome.REDISPLAY;
                }
            }
        }

        return validationOutcome;
    }

    private String validateRequiredElements(List<ValidateElement> elements) {
        String validationOutcome = Outcome.SUCCESS;
        for (ValidateElement validateElement : elements) {
            if (validateElement.isValidateNotNull() && isNullValue(validateElement.getValue(), validateElement.getKey())) {
                validationOutcome = Outcome.REDISPLAY;
            }

            if (validateElement.isValidateRange() && isInvalidRange(validateElement)) {
                validationOutcome = Outcome.REDISPLAY;
            }
        }

        return validationOutcome;
    }

    private void initializeInstance() {
        instance = new FixedAssetPurchaseOrderDetail();
        instance.setTrademarkEntity(null);
        instance.setModelEntity(null);
        instance.putTrademarkName();
        instance.putModelName();
        cleanGroup();

        putDefaultExchangeRates(instance);
    }

    /**
     * Read the default exchange rates and put the values in the <code>FixedAssetPurchaseOrderDetail</code> object.
     * <p/>
     * If cannot read the exchange rates, by default the <code>BigDecimal.ONE</code> is set in the exchange rate fields
     * for  <code>FixedAssetPurchaseOrderDetail</code> object.
     *
     * @param instance The <code>FixedAssetPurchaseOrderDetail</code> where the exchange rate values are established.
     */
    private void putDefaultExchangeRates(FixedAssetPurchaseOrderDetail instance) {
        BigDecimal bsSusRate = BigDecimal.ONE;
        BigDecimal bsUfvRate = BigDecimal.ONE;

        try {
            bsSusRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.warn("Cannot find the currency for: " + FinancesCurrencyType.D.name(), e);
        } catch (FinancesExchangeRateNotFoundException e) {
            log.warn("Cannot find the last exchange rate for: " + FinancesCurrencyType.D.name(), e);
        }

        try {
            bsUfvRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.warn("Cannot find the currency for: " + FinancesCurrencyType.U.name(), e);
        } catch (FinancesExchangeRateNotFoundException e) {
            log.warn("Cannot find the last exchange rate for: " + FinancesCurrencyType.U.name(), e);
        }

        instance.setBsSusRate(bsSusRate);
        instance.setBsUfvRate(bsUfvRate);
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
            System.out.println("****************************************************");
            System.out.println(validateElement.getMoreThan());
            System.out.println(validateElement.getLessThan());
            System.out.println(bigDecimalValue);
            System.out.println("bigDecimalValue.compareTo(validateElement.getMoreThan()) < 0 = " + (bigDecimalValue.compareTo(validateElement.getMoreThan()) < 0));
            System.out.println("bigDecimalValue.compareTo(validateElement.getLessThan()) > 0 = " + (bigDecimalValue.compareTo(validateElement.getLessThan()) > 0));
            if (bigDecimalValue.compareTo(validateElement.getMoreThan()) < 0 || bigDecimalValue.compareTo(validateElement.getLessThan()) > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FixedAssetPurchaseOrderDetail.error.range", message, validateElement.getMoreThan(), validateElement.getLessThan());
                return true;
            }
        }

        return false;
    }

    private String getCompoundRequiredErrorTitle(String generalResourceKey, String resourceKey) {
        MessageUtils.getMessage(generalResourceKey);

        return MessageUtils.getMessage(generalResourceKey) + "(" + MessageUtils.getMessage(resourceKey) + ")";
    }

    private ValidateElement getValidateInstance(Object value, String key) {
        return new ValidateElement(value, key);
    }

    private List<ValidateElement> getDetailRequiredElements() {
        BigDecimal lessThan = BigDecimalUtil.ONE_THOUSAND.subtract(BigDecimal.ONE);
        List<ValidateElement> elements = new ArrayList<ValidateElement>();
        elements.add(getValidateInstance(instance.getRequestedQuantity(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.requestedQuantity")));
        elements.add(getValidateInstance(instance.getBsSusRate(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.exchangeRates", "FinancesExchangeRate.bsSusRate")));
        elements.add(getValidateInstance(instance.getBsUfvRate(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.exchangeRates", "FinancesExchangeRate.bsUfvRate")));
        elements.add(getValidateInstance(instance.getBsUnitPriceValue(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.unitPrices", "Currency.Bs")));
        elements.add(getValidateInstance(instance.getSusUnitPriceValue(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.unitPrices", "Currency.Sus")));
        elements.add(getValidateInstance(instance.getUfvUnitPriceValue(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.unitPrices", "Currency.Ufv")));
        elements.add(getValidateInstance(instance.getFixedAssetSubGroup(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.fixedAssetSubGroup")));
        elements.add(getValidateInstance(instance.getMeasurement(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.measurement")));
        elements.add(getValidateInstance(instance.getDetail(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.detail")));
        elements.add(getValidateInstance(instance.getRubbish(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.rubbish")));
        elements.add(getValidateInstance(instance.getTrademark(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.trademark")));
        elements.add(getValidateInstance(instance.getMonthsGuaranty(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.monthsGuaranty")));
        elements.add(getValidateInstance(instance.getModel(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.model")));
        elements.add(getValidateInstance(instance.getTotalDuration(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.totalDuration")).validateRange(BigDecimal.ZERO, lessThan));
        elements.add(getValidateInstance(instance.getUsageDuration(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.usageDuration")).validateRange(BigDecimal.ZERO, lessThan));
        elements.add(getValidateInstance(instance.getNetDuration(), MessageUtils.getMessage("FixedAssetPurchaseOrderDetail.netDuration")).validateRange(BigDecimal.ZERO, lessThan));
        return elements;
    }

    private List<ValidateElement> getPartRequiredElements() {
        List<ValidateElement> elements = new ArrayList<ValidateElement>();
        elements.add(getValidateInstance(partInstance.getDescription(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.partOptions", "PurchaseOrderDetailPart.description")));
        elements.add(getValidateInstance(partInstance.getUnitPrice(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.partOptions", "PurchaseOrderDetailPart.unitPrice")));
        elements.add(getValidateInstance(partInstance.getMeasureUnit(), getCompoundRequiredErrorTitle("FixedAssetPurchaseOrderDetail.title.partOptions", "PurchaseOrderDetailPart.measureUnit")));

        return elements;
    }

    private class ValidateElement {
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
            return getMoreThan() != null && getLessThan() != null;
        }

        public ValidateElement validateRange(BigDecimal moreThan, BigDecimal lessThan) {
            setMoreThan(moreThan);
            setLessThan(lessThan);
            return this;
        }

    }
}
