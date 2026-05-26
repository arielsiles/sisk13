package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.Vehicle;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de Vehiculos de Transporte.
 * Filtros: placa, marca, color, estado.
 */
@Name("vehicleDataModel")
@Scope(ScopeType.PAGE)
public class VehicleDataModel extends QueryDataModel<Long, Vehicle> {

    private DispatchCatalogState state = DispatchCatalogState.VIG;
    private Long driverIdFilter;

    private static final String[] RESTRICTIONS = {
            "lower(vehicle.plate) like concat('%', concat(lower(#{vehicleDataModel.criteria.plate}), '%'))",
            "lower(vehicle.brand) like concat('%', concat(lower(#{vehicleDataModel.criteria.brand}), '%'))",
            "lower(vehicle.color) like concat('%', concat(lower(#{vehicleDataModel.criteria.color}), '%'))",
            "vehicle.state = #{vehicleDataModel.state}",
            "vehicle.id in (select vh.id from Driver d join d.vehicles vh where d.id = #{vehicleDataModel.driverIdFilter})"
    };

    @Create
    public void init() {
        sortProperty = "vehicle.plate";
    }

    @Override
    public String getEjbql() {
        return "select vehicle from Vehicle vehicle";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public DispatchCatalogState getState() {
        return state;
    }

    public void setState(DispatchCatalogState state) {
        this.state = state;
    }

    public Long getDriverIdFilter() {
        return driverIdFilter;
    }

    public void setDriverIdFilter(Long driverIdFilter) {
        this.driverIdFilter = driverIdFilter;
    }
}
