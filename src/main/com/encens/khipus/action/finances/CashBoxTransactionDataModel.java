package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashBoxTransaction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Data model for Cash box transaction
 *
 * @author:
 */

@Name("cashBoxTransactionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CASHBOXTRANSACTION','VIEW')}")
public class CashBoxTransactionDataModel extends QueryDataModel<Long, CashBoxTransaction> {

    private static final String[] RESTRICTIONS = {
            "cashBoxTransaction.openingDate like concat(#{openingDate}, '%')",
            "cashBoxTransaction.closingDate like concat(#{closingDate}, '%')"};

    @Create
    public void init() {
        sortProperty = "cashBoxTransaction.cashBox.description";
    }

    @Override
    public String getEjbql() {
        return "select cashBoxTransaction from CashBoxTransaction cashBoxTransaction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Factory(value = "openingDate", scope = ScopeType.EVENT)
    public String getOpeningDate() {
        try {
            Date openingDate = getCriteria().getOpeningDate();
            return getStringDate(openingDate);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Factory(value = "closingDate", scope = ScopeType.EVENT)
    public String getClosingDate() {
        try {
            Date openingDate = getCriteria().getClosingDate();
            return getStringDate(openingDate);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String getStringDate(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(GregorianCalendar.YEAR);
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);
        if (month < 10) {
            return "" + year + "-0" + month + "-" + day;
        } else if (day < 10) {
            return "" + year + "-" + month + "-0" + day;
        } else {
            return "" + year + "-" + month + "-" + day;
        }
    }
}

