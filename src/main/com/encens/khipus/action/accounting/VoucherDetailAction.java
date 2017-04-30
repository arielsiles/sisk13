package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.VoucherDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 2.2
 */

@Name("voucherDetailAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherDetailAction extends GenericAction<VoucherDetail> {

    private String clientFullName;

    @Factory(value = "voucherDetail", scope = ScopeType.STATELESS)
    public VoucherDetail initVoucherDetail() {
        return getInstance();
    }


    @Override
    public String create() {
        return Outcome.REDISPLAY;
    }

    @Override
    public String update() {
        return Outcome.REDISPLAY;
    }

    @Override
    public String delete() {
        return Outcome.REDISPLAY;
    }


    public void assignData(){
        System.out.println("VOUCHER DETAIL ACTION. . . . " + getInstance().getClient());
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public void setClientFullName(String clientFullName) {
        this.clientFullName = clientFullName;
    }

    public void printData(){
        System.out.println("___Voucher Detail Print Data__: " + clientFullName);
    }

}
