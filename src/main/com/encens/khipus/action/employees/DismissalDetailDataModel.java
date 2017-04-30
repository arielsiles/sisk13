package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DismissalDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@Name("dismissalDetailDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DISMISSALDETAIL','VIEW')}")
public class DismissalDetailDataModel extends QueryDataModel<Long, DismissalDetail> {
    private static final String[] RESTRICTIONS =
            {
                    "dismissalDetail.code = #{dismissalDetailDataModel.criteria.code}",
            };

    @Create
    public void init() {
        sortProperty = "dismissalDetail.code desc ";
    }

    @Override
    public String getEjbql() {
        return "select dismissalDetail " +
                "from DismissalDetail dismissalDetail ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}