package com.encens.khipus.action.finances.reports;

import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.FinancesCurrency;
import com.encens.khipus.model.finances.RotatoryFundMovementType;
import com.encens.khipus.reports.ReportDesignHelper;
import com.encens.khipus.service.finances.RotatoryFundService;
import com.encens.khipus.util.*;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author
 * @version 3.2.10
 */
public class RotatoryFundBalancesDetailReportScriptlet extends JRDefaultScriptlet {

    public static final String AMOUNT_BY_ROTATORY_FUND_MAP_VAR = "amountByRotatoryFundMapVar";
    public static final String AMOUNT_BY_EMPLOYEE_MAP_VAR = "amountByEmployeeMapVar";
    public static final String MOVEMENT_RESIDUE_AMOUNT_VAR = "movementResidueAmountVar";
    public static final String MOVEMENT_DOCUMENT_NUMBER_VAR = "movementDocumentNumberVar";
    public static final String EMPLOYEE_TOTAL_RESIDUE_AMOUNT_VAR = "employeeTotalResidueAmountVar";
    public static final String BEFORE_RESIDUE_AMOUNT_VAR = "beforeResidueAmountVar";
    public static final String MOVEMENT_BEFORE_RESIDUE_AMOUNT_VAR = "movementBeforeResidueAmountVar";

    public static final String ROTATORY_FUND_SERVICE_PARAMETER = "rotatoryFundService";
    public static final String ACCOUNTING_MOVEMENT_SERVICE_PARAMETER = "accountingMovementService";
    public static final String FINANCE_DOCUMENT_SERVICE_PARAMETER = "financeDocumentService";
    public static final String PAYABLE_DOCUMENT_SERVICE_PARAMETER = "payableDocumentService";
    public static final String BUSINESS_UNIT_ID_PARAMETER = "businessUnitId";
    public static final String ROTATORY_FUND_CODE_PARAMETER = "rotatoryFundCode";
    public static final String MOVEMENT_START_DATE_PARAMETER = "movementStartDate";
    public static final String MOVEMENT_END_DATE_PARAMETER = "movementEndDate";
    public static final String FINANCES_CURRENCY_PARAMETER = "financesCurrency";
    public static final String QUERY_PARAMETERS_PARAMETER = "queryParameters";

    public static final String EMPLOYEE_GROUP = "employeeGroup";
    public static final String ROTATORY_FUND_GROUP = "rotatoryFundGroup";

    public static final String EMPLOYEE_ID_FIELD = "employee.id";
    public static final String DOCUMENT_TYPE_ID_FIELD = "documentType.id";
    public static final String CASH_ACCOUNT_COMPANY_NUMBER_FIELD = "cashAccount.companyNumber";
    public static final String CASH_ACCOUNT_CODE_FIELD = "cashAccount.accountCode";
    public static final String PAYMENT_AMOUNT_FIELD = "rotatoryFundMovement.paymentAmount";
    public static final String COLLECTION_AMOUNT_FIELD = "rotatoryFundMovement.collectionAmount";
    public static final String DOCUMENT_TYPE_CODE_FIELD = "documentType.code";
    public static final String ACCOUNT_CODE_FIELD = "cashAccount.accountCode";
    public static final String ROTATORY_FUND_ID_FIELD = "rotatoryFund.id";
    public static final String ROTATORY_FUND_CODE_FIELD = "rotatoryFund.code";
    public static final String ROTATORY_FUND_MOVEMENT_TRANSACTION_NUMBER_FIELD = "rotatoryFundMovement.transactionNumber";
    public static final String MOVEMENT_TYPE_FIELD = "rotatoryFundMovement.movementType";
    public static final String VOUCHER_TYPE_FIELD = "rotatoryFundMovement.voucherType";
    public static final String VOUCHER_NUMBER_FIELD = "rotatoryFundMovement.voucherNumber";
    public static final String VOUCHER_DATE_FIELD = "rotatoryFundMovement.voucherDate";
    public static final String BANK_PAYMENT_DOCUMENT_TYPE_FIELD = "rotatoryFundMovement.bankPaymentDocumentType";
    public static final String BANK_PAYMENT_DOCUMENT_NUMBER_FIELD = "rotatoryFundMovement.bankPaymentDocumentNumber";
    public static final String CASH_BOX_PAYMENT_DOCUMENT_NUMBER_FIELD = "rotatoryFundMovement.cashBoxPaymentDocumentNumber";
    public static final String CASH_BOX_PAYMENT_ACCOUNT_NUMBER_FIELD = "rotatoryFundMovement.cashBoxPaymentAccountNumber";
    public static final String CASH_BOX_PAYMENT_ACCOUNT_NAME_FIELD = "rotatoryFundMovement.cashBoxPaymentAccountName";
    public static final String DOCUMENT_BANK_COLLECTION_DOCUMENT_TYPE_FIELD = "rotatoryFundMovement.documentBankCollectionDocumentType";
    public static final String DOCUMENT_BANK_COLLECTION_DOCUMENT_NUMBER_FIELD = "rotatoryFundMovement.documentBankCollectionDocumentNumber";
    public static final String DOCUMENT_COLLECTION_DOCUMENT_TYPE_FIELD = "rotatoryFundMovement.documentCollectionDocumentType";
    public static final String DOCUMENT_COLLECTION_DOCUMENT_NUMBER_FIELD = "rotatoryFundMovement.documentCollectionDocumentNumber";
    public static final String CASH_ACCOUNT_ADJUSTMENT_COLLECTION_ACCOUNT_FIELD = "rotatoryFundMovement.cashAccountAdjustmentCollectionAccount";
    public static final String CASH_ACCOUNT_ADJUSTMENT_COLLECTION_NUMBER_FIELD = "rotatoryFundMovement.cashAccountAdjustmentCollectionNumber";
    public static final String DEPOSIT_ADJUSTMENT_COLLECTION_DOCUMENT_TYPE_FIELD = "rotatoryFundMovement.depositAdjustmentCollectionDocumentType";
    public static final String DEPOSIT_ADJUSTMENT_COLLECTION_DOCUMENT_NUMBER_FIELD = "rotatoryFundMovement.depositAdjustmentCollectionDocumentNumber";
    public static final String PURCHASE_ORDER_COLLECTION_ORDER_NUMBER_FIELD = "rotatoryFundMovement.purchaseOrderCollectionOrderNumber";
    public static final String CASH_BOX_COLLECTION_ACCOUNT_NUMBER_FIELD = "rotatoryFundMovement.cashBoxCollectionAccountNumber";
    public static final String CASH_BOX_COLLECTION_ACCOUNT_NAME_FIELD = "rotatoryFundMovement.cashBoxCollectionAccountName";
    public static final String PAYROLL_COLLECTION_NAME_FIELD = "rotatoryFundMovement.payrollCollectionName";


    private Map<String, BigDecimal> amountByRotatoryFundMapVar;
    private Map<String, BigDecimal> amountByEmployeeMapVar;
    private String amountByRotatoryFundKey;
    private String amountByEmployeeKey;


    @Override
    public void afterGroupInit(String groupName) throws JRScriptletException {
        super.afterGroupInit(groupName);
        if (groupName.equals(EMPLOYEE_GROUP)) {
            calculateEmployeeResidueAmount();
        }

        if (groupName.equals(ROTATORY_FUND_GROUP)) {
            prepareBasicValues();
            calculateRotatoryFundBeforeResidueAmount();
        }
    }

    @Override
    public void beforeGroupInit(String groupName) throws JRScriptletException {
        super.beforeGroupInit(groupName);

    }

    @Override
    public void beforeDetailEval() throws JRScriptletException {
        prepareBasicValues();
        formatMovementVariablesValues();

        BigDecimal paymentAmount = ReportDesignHelper.getFieldAsBigDecimal(this, PAYMENT_AMOUNT_FIELD);
        if (!BigDecimalUtil.isZeroOrNull(paymentAmount)) {
            ReportDesignHelper.sumBigDecimalCurrencyMap(amountByRotatoryFundKey, paymentAmount, amountByRotatoryFundMapVar);
            ReportDesignHelper.sumBigDecimalCurrencyMap(amountByEmployeeKey, paymentAmount, amountByEmployeeMapVar);
        }
        BigDecimal collectionAmount = ReportDesignHelper.getFieldAsBigDecimal(this, COLLECTION_AMOUNT_FIELD);
        if (!BigDecimalUtil.isZeroOrNull(collectionAmount)) {
            ReportDesignHelper.subtractBigDecimalCurrencyMap(amountByRotatoryFundKey, collectionAmount, amountByRotatoryFundMapVar);
            ReportDesignHelper.subtractBigDecimalCurrencyMap(amountByEmployeeKey, collectionAmount, amountByEmployeeMapVar);
        }

        setMovementResidueAmountVar();

        super.beforeDetailEval();
    }

    private void calculateEmployeeResidueAmount() throws JRScriptletException {
        RotatoryFundService rotatoryFundService = (RotatoryFundService) getParameterValue(ROTATORY_FUND_SERVICE_PARAMETER);

        Map queryParameters = (Map) getParameterValue(QUERY_PARAMETERS_PARAMETER);

        Long businessUnitId = (Long) queryParameters.get(BUSINESS_UNIT_ID_PARAMETER);
        Integer rotatoryFundCode = (Integer) queryParameters.get(ROTATORY_FUND_CODE_PARAMETER);
        Date movementStartDate = null;//(Date) queryParameters.get(MOVEMENT_START_DATE_PARAMETER);
        Date movementEndDate = (Date) queryParameters.get(MOVEMENT_END_DATE_PARAMETER);
        FinancesCurrency financesCurrency = (FinancesCurrency) queryParameters.get(FINANCES_CURRENCY_PARAMETER);

        Long employeeId = ReportDesignHelper.getFieldAsLong(this, EMPLOYEE_ID_FIELD);
        Long documentTypeId = ReportDesignHelper.getFieldAsLong(this, DOCUMENT_TYPE_ID_FIELD);
        String companyNumber = ReportDesignHelper.getFieldAsString(this, CASH_ACCOUNT_COMPANY_NUMBER_FIELD);
        String cashAccountCode = ReportDesignHelper.getFieldAsString(this, CASH_ACCOUNT_CODE_FIELD);
        BigDecimal residueAmount = rotatoryFundService.calculateResidueAmount(businessUnitId, employeeId, documentTypeId,
                null, rotatoryFundCode, movementStartDate, movementEndDate, financesCurrency, companyNumber, cashAccountCode);
        setVariableValue(EMPLOYEE_TOTAL_RESIDUE_AMOUNT_VAR, residueAmount != null ? residueAmount : BigDecimal.ZERO);
    }


    private void prepareBasicValues() throws JRScriptletException {
        amountByRotatoryFundMapVar = (Map<String, BigDecimal>) getVariableValue(AMOUNT_BY_ROTATORY_FUND_MAP_VAR);
        amountByEmployeeMapVar = (Map<String, BigDecimal>) getVariableValue(AMOUNT_BY_EMPLOYEE_MAP_VAR);
        amountByRotatoryFundKey = FormatUtils.concatBySeparator(Constants.UNDERSCORE_SEPARATOR, getFieldValue(DOCUMENT_TYPE_CODE_FIELD), getFieldValue(ACCOUNT_CODE_FIELD), getFieldValue(EMPLOYEE_ID_FIELD), getFieldValue(ROTATORY_FUND_CODE_FIELD));
        amountByEmployeeKey = FormatUtils.concatBySeparator(Constants.UNDERSCORE_SEPARATOR, getFieldValue(DOCUMENT_TYPE_CODE_FIELD), getFieldValue(CASH_ACCOUNT_CODE_FIELD), getFieldValue(EMPLOYEE_ID_FIELD));
    }

    private void calculateRotatoryFundBeforeResidueAmount() throws JRScriptletException {
        Map queryParameters = (Map) getParameterValue(QUERY_PARAMETERS_PARAMETER);
        BigDecimal beforeResidueAmount = BigDecimal.ZERO;
        BigDecimal movementBeforeResidueAmount = BigDecimal.ZERO;
        // find residue value by movement is necessary

        RotatoryFundService rotatoryFundService = (RotatoryFundService) getParameterValue(ROTATORY_FUND_SERVICE_PARAMETER);
        Long rotatoryFundId = ReportDesignHelper.getFieldAsLong(this, ROTATORY_FUND_ID_FIELD);

        Date movementStartDate = (Date) queryParameters.get(MOVEMENT_START_DATE_PARAMETER);
        Date movementEndDate = (Date) queryParameters.get(MOVEMENT_END_DATE_PARAMETER);
        if (!amountByRotatoryFundMapVar.containsKey(amountByRotatoryFundKey)) {
            if (movementStartDate != null) {
                beforeResidueAmount = rotatoryFundService.getMovementAmountBeforeMovementDate(rotatoryFundId, movementStartDate);

                if (beforeResidueAmount == null) {
                    beforeResidueAmount = BigDecimal.ZERO;
                }
                ReportDesignHelper.sumBigDecimalCurrencyMap(amountByRotatoryFundKey, beforeResidueAmount, amountByRotatoryFundMapVar);
                ReportDesignHelper.sumBigDecimalCurrencyMap(amountByEmployeeKey, beforeResidueAmount, amountByEmployeeMapVar);
            }

            movementBeforeResidueAmount = rotatoryFundService.calculateResidueAmount(null, null, null, rotatoryFundId, null, null, movementEndDate, null, null, null);
        }
        setVariableValue(BEFORE_RESIDUE_AMOUNT_VAR, beforeResidueAmount);
        setVariableValue(MOVEMENT_BEFORE_RESIDUE_AMOUNT_VAR, movementBeforeResidueAmount);


    }

    private void setMovementResidueAmountVar() throws JRScriptletException {
        setVariableValue(MOVEMENT_RESIDUE_AMOUNT_VAR, amountByRotatoryFundMapVar.get(amountByRotatoryFundKey));
    }

    private void formatMovementVariablesValues() throws JRScriptletException {
        String documentNumberVarValue = "";
        String documentNumber = ReportDesignHelper.getFieldAsString(this, DOCUMENT_COLLECTION_DOCUMENT_NUMBER_FIELD);
        if (!ValidatorUtil.isBlankOrNull(documentNumber)) {
            CollectionDocumentType collectionDocumentType = (CollectionDocumentType) getFieldValue(DOCUMENT_COLLECTION_DOCUMENT_TYPE_FIELD);
            documentNumberVarValue = MessageUtils.getMessage(collectionDocumentType.getResourceKey()) + Constants.BLANK_SEPARATOR + documentNumber;
        } else {
            RotatoryFundMovementType movementType = (RotatoryFundMovementType) getFieldValue(MOVEMENT_TYPE_FIELD);
            documentNumberVarValue = MessageUtils.getMessage(movementType.getResourceKey()) + Constants.BLANK_SEPARATOR;

            if (validateField(BANK_PAYMENT_DOCUMENT_TYPE_FIELD)) {
                documentNumberVarValue += formatNumberCode(BANK_PAYMENT_DOCUMENT_TYPE_FIELD, BANK_PAYMENT_DOCUMENT_NUMBER_FIELD);
            } else if (validateField(CASH_BOX_PAYMENT_DOCUMENT_NUMBER_FIELD)) {
                documentNumberVarValue +=
                        ReportDesignHelper.getFieldAsString(this, CASH_BOX_PAYMENT_DOCUMENT_NUMBER_FIELD) +
                                Constants.BLANK_SEPARATOR + formatNumberCode(CASH_BOX_PAYMENT_ACCOUNT_NUMBER_FIELD, CASH_BOX_PAYMENT_ACCOUNT_NAME_FIELD);
            } else if (validateField(DOCUMENT_BANK_COLLECTION_DOCUMENT_TYPE_FIELD)) {
                documentNumberVarValue += formatNumberCode(DOCUMENT_BANK_COLLECTION_DOCUMENT_TYPE_FIELD, DOCUMENT_BANK_COLLECTION_DOCUMENT_NUMBER_FIELD);
            } else if (validateField(CASH_ACCOUNT_ADJUSTMENT_COLLECTION_ACCOUNT_FIELD)) {
                documentNumberVarValue += formatNumberCode(CASH_ACCOUNT_ADJUSTMENT_COLLECTION_ACCOUNT_FIELD, CASH_ACCOUNT_ADJUSTMENT_COLLECTION_NUMBER_FIELD);
            } else if (validateField(DEPOSIT_ADJUSTMENT_COLLECTION_DOCUMENT_TYPE_FIELD)) {
                documentNumberVarValue += formatNumberCode(DEPOSIT_ADJUSTMENT_COLLECTION_DOCUMENT_TYPE_FIELD, DEPOSIT_ADJUSTMENT_COLLECTION_DOCUMENT_NUMBER_FIELD);
            } else if (validateField(PURCHASE_ORDER_COLLECTION_ORDER_NUMBER_FIELD)) {
                documentNumberVarValue += ReportDesignHelper.getFieldAsString(this, PURCHASE_ORDER_COLLECTION_ORDER_NUMBER_FIELD);
            } else if (validateField(CASH_BOX_COLLECTION_ACCOUNT_NUMBER_FIELD)) {
                documentNumberVarValue += formatNumberCode(CASH_BOX_COLLECTION_ACCOUNT_NUMBER_FIELD, CASH_BOX_COLLECTION_ACCOUNT_NAME_FIELD);
            } else if (validateField(PAYROLL_COLLECTION_NAME_FIELD)) {
                documentNumberVarValue += ReportDesignHelper.getFieldAsString(this, PAYROLL_COLLECTION_NAME_FIELD);
            }
        }
        setVariableValue(MOVEMENT_DOCUMENT_NUMBER_VAR, documentNumberVarValue);
    }

    private Boolean validateField(String codeField) throws JRScriptletException {
        return ReportDesignHelper.getFieldAsString(this, codeField) != null;
    }

    private String formatNumberCode(String codeField, String numberField) throws JRScriptletException {
        return ReportDesignHelper.getFieldAsString(this, codeField) + Constants.HYPHEN_SEPARATOR + ReportDesignHelper.getFieldAsString(this, numberField);

    }
}