package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.30
 */
@Name("rotatoryFundDocumentTypeDataModel")
@Scope(ScopeType.PAGE)
public class RotatoryFundDocumentTypeDataModel extends QueryDataModel<Long, RotatoryFundDocumentType> {

    private static final String[] RESTRICTIONS = {
            "rotatoryFundDocumentType.code = #{rotatoryFundDocumentTypeDataModel.criteria.code}",
            "lower(rotatoryFundDocumentType.name) like concat('%',concat(lower(#{rotatoryFundDocumentTypeDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "rotatoryFundDocumentType.code";
    }

    @Override
    public String getEjbql() {
        return "select rotatoryFundDocumentType from RotatoryFundDocumentType rotatoryFundDocumentType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}