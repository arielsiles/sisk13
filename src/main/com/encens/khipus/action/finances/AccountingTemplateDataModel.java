package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.AccountingTemplate;
import com.encens.khipus.model.finances.AccountingTemplatePk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.8
 */
@Name("accountingTemplateDataModel")
@Scope(ScopeType.PAGE)
public class AccountingTemplateDataModel extends QueryDataModel<AccountingTemplatePk, AccountingTemplate> {

    private static final String[] RESTRICTIONS =
            {"lower(accountingTemplate.templateCode) like concat('%', lower(#{accountingTemplateDataModel.criteria.templateCode}))",
                    "lower(accountingTemplate.name) like concat('%', concat(lower(#{accountingTemplateDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "accountingTemplate.templateCode";
    }

    @Override
    public String getEjbql() {
        return "select accountingTemplate from AccountingTemplate accountingTemplate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
