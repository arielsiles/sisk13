package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.XProcess;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer category
 *
 * @author:
 */

@Name("processRegistrationDataModel")
@Scope(ScopeType.PAGE)
//@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
public class ProcessRegistrationDataModel extends QueryDataModel<Long, XProcess> {

    private static final String[] RESTRICTIONS =
            {"lower(xprocess.name) like concat('%', concat(lower(#{processRegistrationDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "xprocess.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select xprocess from XProcess xprocess";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
