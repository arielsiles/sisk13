package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.8
 */
@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "cg_plantillas", schema = Constants.FINANCES_SCHEMA)
public class AccountingTemplate implements BaseModel {

    @EmbeddedId
    private AccountingTemplatePk id = new AccountingTemplatePk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "COD_PLANTI", nullable = false, updatable = false, insertable = false)
    private String templateCode;

    @Column(name = "DESCRI", length = 100)
    private String name;

    @Column(name = "OBS", length = 600)
    private String observation;

    @OneToMany(mappedBy = "accountingTemplate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AccountingTemplateDetail> accountingTemplateDetailList = new ArrayList<AccountingTemplateDetail>(0);


    public AccountingTemplatePk getId() {
        return id;
    }

    public void setId(AccountingTemplatePk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public List<AccountingTemplateDetail> getAccountingTemplateDetailList() {
        return accountingTemplateDetailList;
    }

    public void setAccountingTemplateDetailList(List<AccountingTemplateDetail> accountingTemplateDetailList) {
        this.accountingTemplateDetailList = accountingTemplateDetailList;
    }
}