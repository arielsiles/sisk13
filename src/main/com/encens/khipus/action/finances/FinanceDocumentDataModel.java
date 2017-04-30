package com.encens.khipus.action.finances;

import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.FinanceDocumentPk;
import com.encens.khipus.model.finances.FinanceMovementType;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryCompoundCondition;
import com.encens.khipus.util.query.EntityQueryConditionOperator;
import com.encens.khipus.util.query.EntityQuerySingleCondition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("financeDocumentDataModel")
@Scope(ScopeType.PAGE)
public class FinanceDocumentDataModel extends QueryDataModel<FinanceDocumentPk, FinanceDocument> {
    @In
    RotatoryFundCollectionAction rotatoryFundCollectionAction;
    @Logger
    protected Log log;

    private static final String[] RESTRICTIONS = {
            "documentType.movementType = #{financeDocumentDataModel.movementType}",
            "documentType.voucherType = #{financeDocumentDataModel.voucherType}",
            "lower(concat(financeDocument.documentTypeCode,concat('-',financeDocument.documentNumber))) like concat('%',concat(lower(#{financeDocumentDataModel.criteria.documentNumber}), '%'))",
            "lower(accountingMovement.gloss) like concat('%',concat(lower(#{financeDocumentDataModel.gloss}), '%'))"
    };

    private FinanceMovementType movementType;
    private String voucherType;
    private String gloss;
    private boolean enableRotatoryFundCollectionDepositAdjustment;


    @Create
    public void init() {
        sortProperty = "financeDocument.date";
    }

    @Override
    public String getEjbql() {
        return "select financeDocument from FinanceDocument financeDocument " +
                " left join fetch financeDocument.documentType documentType" +
                " left join fetch financeDocument.accountingMovement accountingMovement" +
                " left join fetch financeDocument.accountingMovement.accountingMovementDetailList accountingMovementDetail" +
                " where financeDocument.state<>#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.FinanceDocumentState','ANL')}" +
                " and financeDocument.provenance=#{'E'} " +
                " and (" +
                " true=#{financeDocumentDataModel.enableRotatoryFundCollectionDepositAdjustment} and " +
                " financeDocument.transactionNumber not in (" +
                " select rfc.depositAdjustmentTransaction from RotatoryFundCollection rfc where rfc.depositAdjustmentTransaction is not null" +
                " and rfc.state<>#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundCollectionState','ANL')})" +
                ")";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String addConditions(String ejbql) {

        EntityQueryCompoundCondition entityQueryCompoundCondition = new EntityQueryCompoundCondition();
        String restrictionResult = "";
        try {
            if (null != rotatoryFundCollectionAction.getDepositInTransitForeignCurrencyAccount()) {
                entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("accountingMovementDetail.account= #{rotatoryFundCollectionAction.depositInTransitForeignCurrencyAccount}"));
                entityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.OR);
            }

            if (null != rotatoryFundCollectionAction.getDepositInTransitNationalCurrencyAccount()) {
                entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("accountingMovementDetail.account= #{rotatoryFundCollectionAction.depositInTransitNationalCurrencyAccount}"));
            }
            restrictionResult = entityQueryCompoundCondition.compile();
        } catch (MalformedEntityQueryCompoundConditionException e) {
            log.error("Malformed entity query compound condition exception, condition will not be added", e);
        }
        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += " and ";
            ejbql += restrictionResult;
        }
        log.debug("ejbql: " + ejbql);
        return ejbql;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public FinanceMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(FinanceMovementType movementType) {
        this.movementType = movementType;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public boolean getEnableRotatoryFundCollectionDepositAdjustment() {
        return enableRotatoryFundCollectionDepositAdjustment;
    }

    public void setEnableRotatoryFundCollectionDepositAdjustment(boolean enableRotatoryFundCollectionDepositAdjustment) {
        this.enableRotatoryFundCollectionDepositAdjustment = enableRotatoryFundCollectionDepositAdjustment;
    }

    public void searchByDepositAdjustment() {
        setEnableRotatoryFundCollectionDepositAdjustment(true);
        setMovementType(FinanceMovementType.D);
        setVoucherType("IN");
        update();
    }
}
