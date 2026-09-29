package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.ContractDuration;
import com.encens.khipus.model.employees.ContractMode;
import com.encens.khipus.model.employees.ContractMovement;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.MainContractResult;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Los actos que cambian la condicion de un contrato.
 * <p/>
 * Son actos y no ediciones de campos: cada uno deja un movimiento con fecha, motivo y usuario.
 * Por eso no viven en el CRUD del contrato.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface ContractConditionService extends GenericService {

    /**
     * Cambia modalidad y/o duracion sobre el MISMO contrato: no se crea uno nuevo ni se cierra
     * el anterior, para que la antiguedad no se corte.
     *
     * @param newEndDate fecha de fin acordada; se ignora si la duracion nueva es indefinida
     */
    ContractMovement changeCondition(Contract contract, ContractMode newMode,
                                     ContractDuration newDuration, Date newEndDate,
                                     Date from, String reason) throws Exception;

    /**
     * Registra la baja: escribe la fecha de fin del contrato, la fecha de salida del empleado y
     * pasa el contrato a INACTIVO.
     * <p/>
     * <b>No toca `activogenplan`.</b> Apagarlo el dia de la salida le haria perder a la persona
     * su ultima planilla, porque se genera el mes siguiente. El filtro por rango de fechas ya la
     * incluye en su ultimo mes y la excluye de los siguientes.
     */
    ContractMovement terminate(Contract contract, Date departureDate, String reason) throws Exception;

    /**
     * Recalcula la fecha de salida del empleado a partir de TODOS sus contratos.
     * <p/>
     * La fecha de salida no es un dato que alguien escriba: es una conclusion. La persona se fue
     * si no le queda ningun contrato abierto. Este es el unico lugar del sistema que escribe esa
     * columna, y por eso hay que llamarlo desde todo camino que cambie un contrato: la baja, el
     * alta, la edicion y el borrado.
     * <p/>
     * La regla:
     * <pre>
     * tiene al menos un contrato NO inactivo  ->  fecha de salida vacia
     * todos sus contratos inactivos           ->  la mayor fecha de fin
     * no tiene contratos                      ->  no se toca
     * </pre>
     * El ultimo caso no es un detalle: hay personas cargadas que todavia no se contrataron, e
     * inventarles una salida seria peor que dejarlas como estan.
     */
    void refreshRetireDate(Employee employee);

    /** Idem, resolviendo el empleado desde el contrato. */
    void refreshRetireDate(Contract contract);

    /**
     * Aplica la regla del contrato principal despues de guardar un contrato, y devuelve que
     * quedo para que la pantalla lo diga.
     * <p/>
     * La regla es <b>como maximo uno</b>, no exactamente uno. Si el contrato viene marcado, le
     * saca la marca a los demas abiertos de esa persona. Si no viene marcado, <b>no designa a
     * nadie</b>: quedarse sin contrato principal es valido -pasa cuando solo quedan eventuales
     * corriendo hasta su fecha de fin- y el sistema no inventa una decision que es de RRHH.
     */
    MainContractResult applyMainContract(Contract contract) throws Exception;

    /** El contrato principal abierto de la persona, o null si no tiene. */
    Contract findMainContract(Employee employee);

    /** true si la persona tiene contratos abiertos y ninguno es principal. */
    boolean lacksMainContract(Employee employee);

    /** Los contratos abiertos de la persona, sin contar el que se pasa. */
    List<Contract> findOpenContracts(Employee employee, Contract excluded);


    List<ContractMovement> findMovements(Contract contract);

    /**
     * @param days dias hacia adelante
     * @return los contratos a plazo fijo que vencen dentro de ese plazo, y los ya vencidos que
     *         nadie resolvio. Un vencido no desaparece: queda a la vista hasta que se decida
     */
    List<Contract> findExpiring(int days);
}
