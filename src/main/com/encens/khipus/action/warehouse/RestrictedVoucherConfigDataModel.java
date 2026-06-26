package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.RestrictedWarehouseVoucherConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado de la pantalla de Restriccion de Vales por usuario.
 */
@Name("restrictedVoucherConfigDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEVOUCHERRESTRICTION','VIEW')}")
public class RestrictedVoucherConfigDataModel extends QueryDataModel<Long, RestrictedWarehouseVoucherConfig> {

    private String username;

    private static final String[] RESTRICTIONS = {
            "lower(restrictedVoucherConfig.user.username) like concat('%', concat(lower(#{restrictedVoucherConfigDataModel.username}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "restrictedVoucherConfig.user.username";
    }

    @Override
    public String getEjbql() {
        return "select restrictedVoucherConfig from RestrictedWarehouseVoucherConfig restrictedVoucherConfig";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
