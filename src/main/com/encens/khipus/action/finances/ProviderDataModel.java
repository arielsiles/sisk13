package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.FinancesEntityState;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.ProviderPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * ProviderDataModel
 *
 * @author
 * @version 2.0
 */
@Name("providerDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW')}")
public class ProviderDataModel extends QueryDataModel<ProviderPk, Provider> {

    private static final String[] RESTRICTIONS = {
            "lower(provider.providerCode) like concat(lower(#{providerDataModel.criteria.providerCode}), '%')",
            "lower(entity.acronym) like concat('%', concat(lower(#{providerDataModel.acronym}), '%'))",
            "provider in (" +
                    "select mp.provider " +
                    "from ModuleProvider mp " +
                    "where mp.moduleProviderType = #{providerDataModel.moduleProviderType}" +
                    ")",
            "entity.state=#{providerDataModel.financesEntityState}"
    };

    private String acronym;
    private ModuleProviderType moduleProviderType;
    private FinancesEntityState financesEntityState;

    @Create
    public void init() {
        sortProperty = "provider.providerCode";
    }

    @Override
    public String getEjbql() {
        return "select provider from Provider provider left join fetch provider.entity entity left join fetch provider.providerClass providerClass";
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public ModuleProviderType getModuleProviderType() {
        return moduleProviderType;
    }

    public void setModuleProviderType(ModuleProviderType moduleProviderType) {
        this.moduleProviderType = moduleProviderType;
    }

    public FinancesEntityState getFinancesEntityState() {
        return financesEntityState;
    }

    public void setFinancesEntityState(FinancesEntityState financesEntityState) {
        this.financesEntityState = financesEntityState;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
