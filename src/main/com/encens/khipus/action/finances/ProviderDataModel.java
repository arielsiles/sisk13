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
// La Transportadora del Despacho es un proveedor y se elige con este datamodel;
// por eso se permite instanciarlo tambien con el permiso de Despacho, de modo que
// un usuario de despachos no requiera WAREHOUSEPROVIDERMAN (que ademas habilita el
// menu/catalogo de Proveedores). La seguridad de cada pantalla sigue en su propio
// restrict (p.ej. dispatchVoucher* -> WAREHOUSEDISPATCH).
@Restrict("#{s:hasPermission('WAREHOUSEPROVIDERMAN','VIEW') or s:hasPermission('WAREHOUSEDISPATCH','VIEW')}")
public class ProviderDataModel extends QueryDataModel<ProviderPk, Provider> {

    private static final String[] RESTRICTIONS = {
            "lower(provider.providerCode) like concat(lower(#{providerDataModel.criteria.providerCode}), '%')",
            "lower(entity.nitNumber) like concat(lower(#{providerDataModel.nit}), '%')",
            "lower(entity.acronym) like concat('%', concat(lower(#{providerDataModel.acronym}), '%'))",
            "provider in (" +
                    "select mp.provider " +
                    "from ModuleProvider mp " +
                    "where mp.moduleProviderType = #{providerDataModel.moduleProviderType}" +
                    ")",
            "entity.state=#{providerDataModel.financesEntityState}"
    };

    private String acronym;
    private String nit;
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

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
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
