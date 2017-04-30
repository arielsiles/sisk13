package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.TaxPercentageType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Tax percentage type
 *
 * @author
 */

@Name("taxPercentageTypeDataModel")
@Scope(ScopeType.PAGE)
public class TaxPercentageTypeDataModel extends QueryDataModel<Long, TaxPercentageType> {

    private static final String[] RESTRICTIONS =
            {"lower(taxPercentageType.name) like concat('%', concat(lower(#{taxPercentageTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "taxPercentageType.name";
    }

    @Override
    public String getEjbql() {
        return "select taxPercentageType from TaxPercentageType taxPercentageType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
