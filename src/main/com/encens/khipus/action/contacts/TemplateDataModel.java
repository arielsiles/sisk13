package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Template;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Encens S.R.L.
 * Template data model
 *
 * @author
 * @version $Id: TemplateDataModel.java  24-feb-2010 18:38:37$
 */
@Name("templateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('TEMPLATE','VIEW')}")
public class TemplateDataModel extends QueryDataModel<Long, Template> {

    private static final String[] RESTRICTIONS =
            {"lower(template.name) like concat('%', concat(lower(#{templateDataModel.criteria.name}), '%'))"};

    @Override
    public String getEjbql() {
        return "select template from Template template";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}