package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens S.R.L.
 * Finances bank entity 
 * @author
 * @version $Id: FinancesBank.java  19-nov-2010 18:27:01$
 */

@NamedQueries({
        @NamedQuery(name = "FinancesBank.bankAccountCrossTabSelect",
                query = "SELECT DISTINCT " +
                        "financesBank.id," +
                        "financesBankAccount.companyNumber," +
                        "financesBankAccount.accountNumber " +
                        " FROM FinancesBank financesBank" +
                        " LEFT JOIN financesBank.financesBankAccountList financesBankAccount" +
                        " LEFT JOIN financesBankAccount.cashAccount cashAccount" +
                        " LEFT JOIN cashAccount.accountingMovementDetailList accountingMovementDetail" +
                        " WHERE financesBankAccount.accountNumber IS NOT NULL"),
        @NamedQuery(name = "FinancesBank.sumAccountingMovementDetailAmount",
                query = "SELECT SUM(accountingMovementDetail.amount / accountingMovementDetail.exchangeRate) " +
                        " FROM FinancesBank financesBank" +
                        " LEFT JOIN financesBank.financesBankAccountList financesBankAccount" +
                        " LEFT JOIN financesBankAccount.cashAccount cashAccount" +
                        " LEFT JOIN cashAccount.accountingMovementDetailList accountingMovementDetail" +
                        " LEFT JOIN accountingMovementDetail.accountingMovement accountingMovement" +
                        " WHERE financesBank.id =:financesBankId " +
                        " AND financesBankAccount.companyNumber =:companyNumber " +
                        " AND financesBankAccount.accountNumber =:accountNumber " +
                        " AND accountingMovement.recordDate <=:currentDate")
})


@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "ck_bancos", schema = Constants.FINANCES_SCHEMA)
public class FinancesBank implements BaseModel {

    @Id
    @Column(name = "COD_BCO", nullable = false, updatable = false, insertable = false)
    private String id;

    @Column(name = "DESCRI", length = 100)
    @Length(max = 100)
    private String name;

    @OneToMany(mappedBy = "financesBank", fetch = FetchType.LAZY)
    private List<FinancesBankAccount> financesBankAccountList = new ArrayList<FinancesBankAccount>(0);


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FinancesBankAccount> getFinancesBankAccountList() {
        return financesBankAccountList;
    }

    public void setFinancesBankAccountList(List<FinancesBankAccount> financesBankAccountList) {
        this.financesBankAccountList = financesBankAccountList;
    }
}
