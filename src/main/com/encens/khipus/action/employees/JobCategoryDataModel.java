package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.JobCategory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for JobCategory
 *
 * @author
 */

@Name("jobCategoryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('JOBCATEGORY','VIEW')}")
public class JobCategoryDataModel extends QueryDataModel<Long, JobCategory> {
    private static final String[] RESTRICTIONS =
            {"lower(jobCategory.name) like concat('%', concat(lower(#{jobCategoryDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "jobCategory.name";
    }

    @Override
    public String getEjbql() {
        return "select jobCategory from JobCategory jobCategory" +
                " left join fetch jobCategory.sector sector" +
                " left join fetch jobCategory.nationalCurrencyDebitAccount nationalCurrencyDebitAccount" +
                " left join fetch jobCategory.nationalCurrencyCreditAccount nationalCurrencyCreditAccount" +
                " left join fetch jobCategory.foreignCurrencyDebitAccount foreignCurrencyDebitAccount" +
                " left join fetch jobCategory.foreignCurrencyCreditAccount foreignCurrencyCreditAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}