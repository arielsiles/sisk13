package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Title;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author
 */
@Name("titleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('TITLE','VIEW')}")
public class TitleDataModel extends QueryDataModel<Long, Title> {

    private static final String[] RESTRICTIONS =
            {"lower(title.name) like concat('%', concat(lower(#{titleDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "title.name";
    }

    @Override
    public String getEjbql() {
        return "select title from Title title";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
