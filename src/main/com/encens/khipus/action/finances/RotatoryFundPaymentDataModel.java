package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.RotatoryFundPayment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.23
 */
@Name("rotatoryFundPaymentDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','VIEW')}")
public class RotatoryFundPaymentDataModel extends QueryDataModel<Long, RotatoryFundPayment> {
    private static final String[] RESTRICTIONS = {
            "rotatoryFundPayment.rotatoryFund = #{rotatoryFund}"
    };

    @Create
    public void init() {
        sortProperty = "rotatoryFundPayment.creationDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select rotatoryFundPayment from RotatoryFundPayment rotatoryFundPayment";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}