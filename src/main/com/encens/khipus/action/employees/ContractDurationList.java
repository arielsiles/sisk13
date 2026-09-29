package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.ContractDuration;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Las opciones de duracion, para los combos.
 * <p/>
 * Es un componente propio y no un metodo de la accion del contrato porque lo usan tres pantallas
 * distintas -alta de contrato, cambio de condicion y el listado- y ninguna deberia depender de
 * la conversacion de otra.
 *
 * @author
 * @version 6.1.0
 */
@Name("contractDurationListHolder")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ContractDurationList implements Serializable {

    @Factory(value = "contractDurationList", scope = ScopeType.STATELESS)
    public List<ContractDuration> getContractDurationList() {
        return Arrays.asList(ContractDuration.values());
    }
}
