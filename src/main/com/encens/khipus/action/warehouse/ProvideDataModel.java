package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Provide;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("provideDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
public class ProvideDataModel extends QueryDataModel<Long, Provide> {
    private static final String[] RESTRICTIONS = {
            "provide.companyNumber = #{providerAction.provider.companyNumber}",
            "provide.providerCode = #{providerAction.provider.providerCode}"
    };

    @Create
    public void init() {
        sortProperty = "provide.productItem.name";
    }

    @Override
    public String getEjbql() {
        return "select provide from Provide provide " +
                " left join fetch provide.productItem productItem" +
                " left join fetch provide.groupMeasureUnit groupMeasureUnit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
