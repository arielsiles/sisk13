package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.TaxPercentage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * TaxPercentageDataModel
 *
 * @author:
 */
@Name("taxPercentageDataModel")
@Scope(ScopeType.PAGE)
public class TaxPercentageDataModel extends QueryDataModel<Long, TaxPercentage> {

    private static final String[] RESTRICTIONS =
            {"lower(taxPercentage.description) like concat('%', concat(lower(#{taxPercentageDataModel.criteria.description}), '%'))"};

    @Create
    public void init() {
        sortProperty = "taxPercentage.description";
    }

    @Override
    public String getEjbql() {
        return "select taxPercentage from TaxPercentage taxPercentage";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
