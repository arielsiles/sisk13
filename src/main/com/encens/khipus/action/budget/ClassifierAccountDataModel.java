package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.ClassifierAccount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ClassifierAccountDataModel
 *
 * @author
 * @version 2.1
 */
@Name("classifierAccountDataModel")
@Scope(ScopeType.PAGE)
public class ClassifierAccountDataModel extends QueryDataModel<Long, ClassifierAccount> {
    private static final String[] RESTRICTIONS = {"classifierAccount.classifier =  #{classifierAccountDataModel.criteria.classifier}"};

    @Create
    public void init() {
        sortProperty = "classifierAccount.accountCode";
    }

    @Override
    public String getEjbql() {
        return "select classifierAccount from ClassifierAccount classifierAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
