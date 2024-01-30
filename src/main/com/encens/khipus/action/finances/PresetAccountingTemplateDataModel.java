package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
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

@Name("presetAccountingTemplateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
public class PresetAccountingTemplateDataModel extends QueryDataModel<Long, PresetAccountingTemplate> {

    private static final String[] RESTRICTIONS =
            {"lower(presetAccountingTemplate.name) like concat('%', concat(lower(#{presetAccountingTemplateDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "presetAccountingTemplate.name";
    }

    @Override
    public String getEjbql() {
        return "select presetAccountingTemplate from PresetAccountingTemplate presetAccountingTemplate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
