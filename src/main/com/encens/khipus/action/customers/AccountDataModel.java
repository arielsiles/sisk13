package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Account;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model for Account
 *
 * @author:
 */

@Name("accountDataModel")
@Scope(ScopeType.PAGE)
/*@Restrict("#{s:hasPermission('CREDIT','VIEW')}")*/
public class AccountDataModel extends QueryDataModel<Long, Account> {

    private String firstName;
    private String lastName;
    private String maidenName;

    /** Rango de fechavence. Van como campos propios porque criteria solo admite un valor. */
    private Date expirationFrom;
    private Date expirationTo;

    private static final String[] RESTRICTIONS = {
            "lower(account.partner.firstName) like concat('%', concat(lower(#{accountDataModel.firstName}), '%'))",
            "lower(account.partner.lastName) like concat('%', concat(lower(#{accountDataModel.lastName}), '%'))",
            "lower(account.partner.maidenName) like concat('%', concat(lower(#{accountDataModel.maidenName}), '%'))",
            "lower(account.code) like concat('%', concat(lower(#{accountDataModel.criteria.code}), '%'))",
            "lower(account.accountNumber) like concat('%', concat(lower(#{accountDataModel.criteria.accountNumber}), '%'))",
            "account.accountState = #{accountDataModel.criteria.accountState}",
            "account.accountType = #{accountDataModel.criteria.accountType}",
            "account.expirationDate >= #{accountDataModel.expirationFrom}",
            "account.expirationDate <= #{accountDataModel.expirationTo}"
    };

    /**
     * Sin ORDER BY la paginacion no es estable: la misma cuenta puede repetirse o
     * saltearse entre paginas. Se arranca por codigo y los encabezados de Apertura y
     * Vencimiento cambian el criterio.
     */
    @Create
    public void init() {
        sortProperty = "account.code";
    }

    @Override
    public String getEjbql() {
        return "select account from Account account";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public Date getExpirationFrom() {
        return expirationFrom;
    }

    public void setExpirationFrom(Date expirationFrom) {
        this.expirationFrom = expirationFrom;
    }

    public Date getExpirationTo() {
        return expirationTo;
    }

    public void setExpirationTo(Date expirationTo) {
        this.expirationTo = expirationTo;
    }
}
