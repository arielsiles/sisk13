package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.DispatchCatalogState;
import com.encens.khipus.model.warehouse.InventoryPackaging;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de Tipos de Bolsa/Envase.
 * Filtros: nombre, etiqueta de capacidad, estado.
 */
@Name("inventoryPackagingDataModel")
@Scope(ScopeType.PAGE)
public class InventoryPackagingDataModel extends QueryDataModel<Long, InventoryPackaging> {

    private DispatchCatalogState state = DispatchCatalogState.VIG;

    private static final String[] RESTRICTIONS = {
            "lower(inventoryPackaging.name) like concat('%', concat(lower(#{inventoryPackagingDataModel.criteria.name}), '%'))",
            "lower(inventoryPackaging.capacityLabel) like concat('%', concat(lower(#{inventoryPackagingDataModel.criteria.capacityLabel}), '%'))",
            "inventoryPackaging.state = #{inventoryPackagingDataModel.state}"
    };

    @Create
    public void init() {
        sortProperty = "inventoryPackaging.name";
    }

    public DispatchCatalogState getState() {
        return state;
    }

    public void setState(DispatchCatalogState state) {
        this.state = state;
    }

    @Override
    public String getEjbql() {
        return "select inventoryPackaging from InventoryPackaging inventoryPackaging";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
