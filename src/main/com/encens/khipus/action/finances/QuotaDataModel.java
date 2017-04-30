package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Quota;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.22
 */

@Name("quotaDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('QUOTA','VIEW')}")
public class QuotaDataModel extends QueryDataModel<Long, Quota> {
    private static final String[] RESTRICTIONS = {
            /* since this is a nested conversation that allows to view both entities in the same form
            * we select the instance which evolves the other*/
            "quota.rotatoryFund = #{rotatoryFund}"
    };

    @Create
    public void init() {
        sortProperty = "quota.expirationDate";
    }

    @Override
    public String getEjbql() {
        return "select quota from Quota quota";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}