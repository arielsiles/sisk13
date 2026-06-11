package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.DispatchRoute;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de Rutas de Despacho.
 * Filtros: nombre, origen, destino, estado.
 */
@Name("dispatchRouteDataModel")
@Scope(ScopeType.PAGE)
public class DispatchRouteDataModel extends QueryDataModel<Long, DispatchRoute> {

    private CatalogApprovalState state;

    private static final String[] RESTRICTIONS = {
            "lower(dispatchRoute.name) like concat('%', concat(lower(#{dispatchRouteDataModel.criteria.name}), '%'))",
            "lower(dispatchRoute.originText) like concat('%', concat(lower(#{dispatchRouteDataModel.criteria.originText}), '%'))",
            "lower(dispatchRoute.destinationText) like concat('%', concat(lower(#{dispatchRouteDataModel.criteria.destinationText}), '%'))",
            "dispatchRoute.state = #{dispatchRouteDataModel.state}"
    };

    @Create
    public void init() {
        sortProperty = "dispatchRoute.name";
    }

    @Override
    public String getEjbql() {
        return "select dispatchRoute from DispatchRoute dispatchRoute";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public CatalogApprovalState getState() {
        return state;
    }

    public void setState(CatalogApprovalState state) {
        this.state = state;
    }
}
