package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.sales.CommercialCostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for commercial cost center catalog.
 *
 * @author
 * @version 1.0
 */
@Name("commercialCostCenterDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('COMMERCIALCOSTCENTER','VIEW')}")
public class CommercialCostCenterDataModel extends QueryDataModel<Long, CommercialCostCenter> {

    private static final String[] RESTRICTIONS = {
            "lower(commercialCostCenter.code) like concat(lower(#{commercialCostCenterDataModel.criteria.code}), '%')",
            "lower(commercialCostCenter.description) like concat('%', concat(lower(#{commercialCostCenterDataModel.criteria.description}), '%'))"};

    @Create
    public void init() {
        sortProperty = "commercialCostCenter.code";
    }

    @Override
    public String getEjbql() {
        return "select commercialCostCenter from CommercialCostCenter commercialCostCenter";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
