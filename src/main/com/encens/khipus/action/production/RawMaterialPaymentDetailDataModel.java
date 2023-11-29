package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.RawMaterialPaymentDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("rawMaterialPaymentDetailDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialPaymentDetailDataModel extends QueryDataModel<Long, RawMaterialPaymentDetail> {

    private static final String[] RESTRICTIONS = {
            ""
    };

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select rawMaterialPaymentDetail from RawMaterialPaymentDetail rawMaterialPaymentDetail";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
