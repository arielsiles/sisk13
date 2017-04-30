package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.PreBookEntryPeriodType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Pre book entry period type
 *
 * @author:
 */

@Name("preBookEntryPeriodTypeDataModel")
@Scope(ScopeType.PAGE)
public class PreBookEntryPeriodTypeDataModel extends QueryDataModel<Long, PreBookEntryPeriodType> {

    private static final String[] RESTRICTIONS =
            {"lower(preBookEntryPeriodType.name) like concat('%', concat(lower(#{preBookEntryPeriodTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "preBookEntryPeriodType.name";
    }

    @Override
    public String getEjbql() {
        return "select preBookEntryPeriodType from PreBookEntryPeriodType preBookEntryPeriodType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
