package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.RawMaterialPayment;
import com.encens.khipus.model.production.RawMaterialPaymentDetail;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

@Name("rawMaterialPaymentAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialPaymentAction extends GenericAction<RawMaterialPayment> {

    private List<RawMaterialPaymentDetail> paymentDetails = new ArrayList<RawMaterialPaymentDetail>();

    @Factory(value = "rawMaterialPayment", scope = ScopeType.STATELESS)
    public RawMaterialPayment initRawMaterialPayment() {
        return getInstance();
    }

    @Override
    @End
    public String create() {
        try {
            RawMaterialPayment rawMaterialPayment = getInstance();


            getService().create(rawMaterialPayment);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

        return Outcome.SUCCESS;
    }

    public void assignBeneficiary(RawMaterialProducer rawMaterialProducerItem){
        getInstance().setProducer(rawMaterialProducerItem);
    }

    public void clearRawMaterialProducer() {
        getInstance().setProducer(null);
    }

    public List<RawMaterialPaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<RawMaterialPaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
