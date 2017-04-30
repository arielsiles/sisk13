package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.PartnerClubCategory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Document type
 *
 * @author:
 */
@Name("partnerClubCategoryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PARTNERCLUBCATEGORY','VIEW')}")
public class PartnerClubCategoryDataModel extends QueryDataModel<Long, PartnerClubCategory> {

    private static final String[] RESTRICTIONS =
            {"lower(partnerClubCategory.name) like concat('%', concat(lower(#{partnerClubCategoryDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "partnerClubCategory.name";
    }

    @Override
    public String getEjbql() {
        return "select partnerClubCategory from PartnerClubCategory partnerClubCategory";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
