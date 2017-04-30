package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.RHMark;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : LoginRHMarkDataModel, 23-10-2009 12:26:18 PM
 */
@Name("loginRHMarkDataModel")
@Scope(ScopeType.PAGE)
public class LoginRHMarkDataModel extends QueryDataModel<Long, RHMark> {

    private static final String[] RESTRICTIONS =
            {"rHMark.company.id = #{loginRHMarkDataModel.defaultCompany}",
                    "rHMark.marRefCard = #{loginRHMarkDataModel.criteria.marRefCard}",
                    "rHMark.marDate >= #{loginRHMarkDataModel.criteria.startMarDate}",
                    "rHMark.marDate <= #{loginRHMarkDataModel.criteria.endMarDate}"};

    private Long defaultCompany = new Long(1);

    public Long getDefaultCompany() {
        return defaultCompany;
    }

    public void setDefaultCompany(Long defaultCompany) {
        this.defaultCompany = defaultCompany;
    }

    @Create
    public void init() {
        sortProperty = "rHMark.marDate, rHMark.marTime";
    }


    @Override
    public String getEjbql() {
        return "select rHMark from RegisterMarkAction rHMark";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void search() {
        super.search();
    }


}