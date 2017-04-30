package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.DosageType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Dosage type
 *
 * @author:
 */

@Name("dosageTypeDataModel")
@Scope(ScopeType.PAGE)
public class DosageTypeDataModel extends QueryDataModel<Long, DosageType> {

    private static final String[] RESTRICTIONS =
            {"lower(dosageType.name) like concat('%', concat(lower(#{dosageTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "dosageType.name";
    }

    @Override
    public String getEjbql() {
        return "select dosageType from DosageType dosageType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
