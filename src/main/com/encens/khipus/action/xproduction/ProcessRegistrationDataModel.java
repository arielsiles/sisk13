package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.xproduction.XProcess;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

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
            {"lower(xProcess.name) like concat('%', concat(lower(#{processRegistrationDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "xProcess.name";
    }

    @Override
    public String getEjbql() {
        return "select xProcess from XProcess xProcess";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
