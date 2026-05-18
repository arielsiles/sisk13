package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.DispatchPlace;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo Lugares de Despacho/Entrega.
 */
@Name("dispatchPlaceDataModel")
@Scope(ScopeType.PAGE)
public class DispatchPlaceDataModel extends QueryDataModel<Long, DispatchPlace> {

    private Boolean active;

    private static final String[] RESTRICTIONS = {
            "lower(dispatchPlace.code) like concat('%', concat(lower(#{dispatchPlaceDataModel.criteria.code}), '%'))",
            "lower(dispatchPlace.description) like concat('%', concat(lower(#{dispatchPlaceDataModel.criteria.description}), '%'))",
            "dispatchPlace.kind = #{dispatchPlaceDataModel.criteria.kind}",
            "dispatchPlace.active = #{dispatchPlaceDataModel.active}"
    };

    @Create
    public void init() {
        sortProperty = "dispatchPlace.code";
        active = Boolean.TRUE;
    }

    @Override
    public String getEjbql() {
        return "select dispatchPlace from DispatchPlace dispatchPlace";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
