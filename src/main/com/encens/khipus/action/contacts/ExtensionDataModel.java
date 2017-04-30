package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Extension;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Extension
 *
 * @author
 * @version 2.7
 */

@Name("extensionDataModel")
@Scope(ScopeType.PAGE)
public class ExtensionDataModel extends QueryDataModel<Long, Extension> {

    private static final String[] RESTRICTIONS =
            {"extension.documentType = #{documentType}",
                    "lower(extension.extension) like concat(lower(#{extensionDataModel.criteria.extension}), '%')"};

    @Create
    public void init() {
        sortProperty = "extension.extension";
    }

    @Override
    public String getEjbql() {
        return "select extension from Extension extension";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}