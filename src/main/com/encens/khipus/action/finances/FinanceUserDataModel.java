package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.FinanceUser;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.5.3
 */

@Name("financeUserDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FINANCEUSER','VIEW')}")
public class FinanceUserDataModel extends QueryDataModel<Long, FinanceUser> {
    private static final String[] RESTRICTIONS =
            {
                    "lower(financeUser.name) like concat('%',lower(#{financeUserDataModel.criteria.name}),'%')",
                    "financeUser.oracleUser = #{financeUserDataModel.criteria.oracleUser}"
            };

    @Create
    public void init() {
        sortProperty = "financeUser.name";
    }

    @Override
    public String getEjbql() {
        return "select financeUser from FinanceUser financeUser";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}