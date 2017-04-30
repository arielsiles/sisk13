package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.FinancesEntity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.5
 */
@Name("financesEntityDataModel")
@Scope(ScopeType.PAGE)
public class FinancesEntityDataModel extends QueryDataModel<String, FinancesEntity> {
    private static final String[] RESTRICTIONS = {
            "lower(financesEntity.code) like concat(lower(#{financesEntityDataModel.criteria.id}), '%')",
            "lower(financesEntity.acronym) like concat('%', concat(lower(#{financesEntityDataModel.criteria.acronym}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "financesEntity.acronym";
    }

    @Override
    public String getEjbql() {
        return "select financesEntity from FinancesEntity financesEntity";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
