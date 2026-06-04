package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel para el popup de seleccion de Cliente desde el formulario de
 * Vale de Despacho. Por seguridad de informacion solo expone los campos
 * codPrefijo y codigo (codigocliente), filtrables. No muestra nombre,
 * apellidos ni datos comerciales.
 */
@Name("dispatchClientDataModel")
@Scope(ScopeType.PAGE)
public class DispatchClientDataModel extends QueryDataModel<Long, Client> {

    // Cada restriction de Seam debe tener exactamente UNA expresion #{...};
    // el filtro fijo "client.codigo is not null" va en el WHERE base del
    // EJB-QL (no en restrictions).
    private static final String[] RESTRICTIONS = {
            "lower(client.codPrefijo) like concat('%', concat(lower(#{dispatchClientDataModel.criteria.codPrefijo}), '%'))",
            "lower(client.codigo) like concat('%', concat(lower(#{dispatchClientDataModel.criteria.codigo}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "client.codigo";
    }

    @Override
    public String getEjbql() {
        return "select client from Client client where client.codigo is not null";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
