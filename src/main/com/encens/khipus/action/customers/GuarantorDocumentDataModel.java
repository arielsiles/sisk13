package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.GuarantorDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Credit
 *
 * @author:
 */

@Name("guarantorDocumentDataModel")
@Scope(ScopeType.PAGE)
public class GuarantorDocumentDataModel extends QueryDataModel<Long, GuarantorDocument> {

    private static final String[] RESTRICTIONS =
            {"guarantorDocument.guarantor.credit = #{credit}"};

    @Override
    public String getEjbql() {
        return "select guarantorDocument from GuarantorDocument guarantorDocument";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
