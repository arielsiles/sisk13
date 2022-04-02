package com.encens.khipus.model.customers;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 18/12/13
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class AccountItemPK implements Serializable {

    @Column(name = "id_cuenta", nullable = false, length = 10)
    @Length(max = 10)
    private Integer idAccount;

    @Column(name = "cod_art", nullable = false, length = 6)
    @Length(max = 6)
    private String codArt;

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccountItemPK accountItemPK = (AccountItemPK) o;

        if (!accountItemPK.equals(accountItemPK.idAccount)) {
            return false;
        }

        if (!accountItemPK.equals(accountItemPK.codArt)) {
            return false;
        }
        if (!accountItemPK.equals(accountItemPK.companyNumber)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = idAccount.hashCode();
        result = 31 * result + companyNumber.hashCode() + codArt.hashCode();
        return result;
    }

    public Integer getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Integer idAccount) {
        this.idAccount = idAccount;
    }

    public String getCodArt() {
        return codArt;
    }

    public void setCodArt(String codArt) {
        this.codArt = codArt;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
}
