package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.customers.CreditTransactionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("creditAction")
@Scope(ScopeType.CONVERSATION)
public class CreditAction extends GenericAction<Credit> {

    @In
    private CreditTransactionService creditTransactionService;

    @In
    private CreditService creditService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In(create = true)
    private CreditTransactionAction creditTransactionAction;

    private BigDecimal totalPayment;

    private Date paymentDate = new Date();

    @Factory(value = "credit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CREDIT','VIEW')}")
    public Credit initCredit() {
        return getInstance();
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Credit instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    @Override
    @End
    public String create() {
        Credit instance = getInstance();
        Partner partner = instance.getPartner();
        Integer num = partner.getNumberCredit()+1;
        String creditNumber = partner.getProductiveZone().getNumber()+'-'+partner.getNumber()+'-'+num;
        getInstance().setCode(creditNumber);
        getInstance().setState(CreditState.VIG);
        getInstance().setCapitalBalance(instance.getAmount());
        super.create();
        sequenceGeneratorService.nextCreditNumber(getInstance().getPartner().getId());
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addCreditPayment() {
        setOp(OP_UPDATE);
        return creditTransactionAction.addCreditTransaction();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addPayoutCredit() {
        setOp(OP_UPDATE);
        return creditTransactionAction.addCreditTransactionPayout();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String selectCreditTransaction(CreditTransaction creditTransaction) {
        setOp(OP_UPDATE);
        return creditTransactionAction.select(creditTransaction);
    }

    public void checkCreditStatus(){

        for (Credit credit : creditService.getAllCredits()){

        }

    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }
}
