package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.DispatchObservation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de Observaciones de Despacho.
 * Filtros: nombre descriptivo, contenido de la observacion, estado.
 */
@Name("dispatchObservationDataModel")
@Scope(ScopeType.PAGE)
public class DispatchObservationDataModel extends QueryDataModel<Long, DispatchObservation> {

    private CatalogApprovalState state;
    private String nameFilter;
    private String observationFilter;

    private static final String[] RESTRICTIONS = {
            "lower(dispatchObservation.name) like concat('%', concat(lower(#{dispatchObservationDataModel.nameFilter}), '%'))",
            "lower(dispatchObservation.observation) like concat('%', concat(lower(#{dispatchObservationDataModel.observationFilter}), '%'))",
            "dispatchObservation.state = #{dispatchObservationDataModel.state}"
    };

    @Create
    public void init() {
        sortProperty = "dispatchObservation.name";
        sortAsc = true;
    }

    @Override
    public String getEjbql() {
        return "select dispatchObservation from DispatchObservation dispatchObservation";
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

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    public String getObservationFilter() {
        return observationFilter;
    }

    public void setObservationFilter(String observationFilter) {
        this.observationFilter = observationFilter;
    }
}
