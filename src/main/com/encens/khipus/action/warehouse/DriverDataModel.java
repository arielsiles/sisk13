package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.Driver;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de Conductores.
 * Filtros: nombre, licencia, estado.
 */
@Name("driverDataModel")
@Scope(ScopeType.PAGE)
public class DriverDataModel extends QueryDataModel<Long, Driver> {

    private DispatchCatalogState state = DispatchCatalogState.VIG;

    private static final String[] RESTRICTIONS = {
            "lower(driver.name) like concat('%', concat(lower(#{driverDataModel.criteria.name}), '%'))",
            "lower(driver.license) like concat('%', concat(lower(#{driverDataModel.criteria.license}), '%'))",
            "driver.state = #{driverDataModel.state}"
    };

    @Create
    public void init() {
        sortProperty = "driver.name";
    }

    public DispatchCatalogState getState() {
        return state;
    }

    public void setState(DispatchCatalogState state) {
        this.state = state;
    }

    @Override
    public String getEjbql() {
        return "select driver from Driver driver";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
