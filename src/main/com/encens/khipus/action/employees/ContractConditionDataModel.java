package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ContractDuration;
import com.encens.khipus.model.employees.ContractState;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Los contratos con su condicion. Trae el filtro de vencimiento, que es la razon de ser de la
 * pantalla: que nadie se entere de un vencimiento porque la planilla dejo afuera a alguien.
 * <p/>
 * La pantalla sirve para dos cosas que tiran para lados opuestos: atender los vencimientos de
 * hoy y buscar un contrato de hace anios. Por eso todo filtro tiene un estado neutro y el unico
 * que arranca puesto es el estado del contrato.
 *
 * @author
 * @version 6.1.0
 */
@Name("contractConditionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CONTRACTCONDITION','VIEW')}")
public class ContractConditionDataModel extends QueryDataModel<Long, Contract> {

    private static final String[] RESTRICTIONS = {
            /* Busca en nombre y los dos apellidos: la etiqueta dice "Persona", asi que
               filtrar solo por el paterno era mentir. Se concatena y se compara una vez
               para no repetir la expresion EL, y con coalesce porque un apellido materno
               nulo anularia toda la concatenacion. */
            "lower(concat(concat(concat(coalesce(employee.firstName,''),' '),concat(coalesce(employee.lastName,''),' ')),coalesce(employee.maidenName,''))) like concat('%', concat(lower(#{contractConditionDataModel.employeeName}), '%'))",
            "contract.duration = #{contractConditionDataModel.duration}",
            "contract.activeForPayrollGeneration = #{contractConditionDataModel.activeForPayroll}",
            "upper(contractState.name) = #{contractConditionDataModel.contractStateName}",

            /* Vencimiento sin resolver. Son tres condiciones y no una: la fecha de fin guarda
               tanto el plazo pactado como el dia en que la persona se fue, y sin separarlas el
               filtro devolvia contratos cerrados hace anios como si hubiera algo que decidir.
               Las tres se encienden y se apagan juntas con la casilla. */
            "contract.endDate <= #{contractConditionDataModel.expiringLimit}",
            "contract.duration = #{contractConditionDataModel.expiringDuration}",
            "upper(contractState.name) <> #{contractConditionDataModel.expiringExcludedState}",

            /* Vigente en una fecha. Van separadas porque cada restriccion se apaga por su propia
               expresion nula, y una sola con el 'or' adentro llevaria dos. */
            "contract.initDate <= #{contractConditionDataModel.validOn}",
            "(contract.endDate is null or contract.endDate >= #{contractConditionDataModel.validOn})"};

    /** El contrato ya cerrado. Es el mismo nombre con el que lo marca "Dar de baja". */
    private static final String INACTIVE_STATE = ContractState.INACTIVE;
    private static final String ACTIVE_STATE = "ACTIVO";

    private String employeeName;
    private ContractDuration duration;

    /* Sin valor inicial: con el filtro puesto de fabrica, quien busca por nombre y no encuentra
       concluye que el contrato no existe, en vez de sospechar del filtro que nunca toco. */
    private Boolean activeForPayroll;
    private Date validOn;

    /* El estado se guarda por nombre y no como entidad: el conversor de entidades empareja el
       valor elegido contra los items usando su propio contexto de persistencia, y una instancia
       traida por otro entity manager no coincide con ninguno, asi que el combo se veia vacio
       aunque el filtro estuviera aplicado. Con el nombre no hay conversor de por medio. */
    private String contractStateName;
    private List<SelectItem> stateItems;

    /* Cuando esta en true, solo los que vencen dentro de expiringDays -y los ya vencidos-. */
    private boolean onlyExpiring = false;
    private int expiringDays = 30;

    @Create
    public void init() {
        sortProperty = "employee.lastName";
        contractStateName = defaultStateName();
    }

    /**
     * Los estados para el combo, con la opcion en blanco arriba. `estadocontrato` es una tabla
     * por empresa y no un enumerado, asi que los nombres salen de la base.
     */
    public List<SelectItem> getStateItems() {
        if (null == stateItems) {
            stateItems = new ArrayList<SelectItem>();
            stateItems.add(new SelectItem(null, ""));
            for (String name : findStateNames()) {
                stateItems.add(new SelectItem(name.toUpperCase(), name));
            }
        }
        return stateItems;
    }

    /**
     * ACTIVO si esa empresa lo tiene escrito asi, y null si no. Devolver null deja el filtro
     * neutro: ante la duda se ve de mas, nunca de menos.
     */
    private String defaultStateName() {
        for (String name : findStateNames()) {
            if (ACTIVE_STATE.equalsIgnoreCase(name.trim())) {
                return ACTIVE_STATE;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private List<String> findStateNames() {
        try {
            List<String> names = getEntityManager()
                    .createQuery("select o.name from ContractState o where o.name is not null"
                            + " order by o.name")
                    .getResultList();
            return null == names ? new ArrayList<String>() : names;
        } catch (Exception e) {
            return new ArrayList<String>();
        }
    }

    @Override
    public String getEjbql() {
        return "select contract from Contract contract"
                + " left join fetch contract.employee employee"
                + " left join fetch contract.contractMode contractMode"
                + " left join fetch contract.contractState contractState";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    /**
     * Devuelve null cuando el filtro esta apagado: una restriccion con valor nulo no se aplica,
     * asi el mismo listado sirve para ver todo o solo lo que vence.
     */
    public Date getExpiringLimit() {
        if (!onlyExpiring) {
            return null;
        }
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, expiringDays);
        return limit.getTime();
    }

    /** Solo el plazo fijo vence: un indefinido no tiene nada que renovar. */
    public ContractDuration getExpiringDuration() {
        return onlyExpiring ? ContractDuration.FIXED_TERM : null;
    }

    /**
     * Deja afuera los que ya se resolvieron dando de baja. Con esto las tres salidas -ampliar,
     * pasar a indefinido y dar de baja- sacan el contrato de la lista por si solas, que es lo
     * que hace que la lista sirva como bandeja de trabajo pendiente.
     */
    public String getExpiringExcludedState() {
        return onlyExpiring ? INACTIVE_STATE : null;
    }

    /**
     * Vuelve a leer la lista despues de cambiar un contrato.
     * <p/>
     * Hacen falta las dos cosas. `updateAndSearch` tira la cuenta de filas y las filas que el
     * modelo tenia guardadas; sin eso el reRender vuelve a dibujar la fila vieja. Y `clear` del
     * entity manager porque la baja la escribe el servicio con SU contexto de persistencia: el
     * de la lista sigue teniendo el contrato como estaba, y Hibernate devolveria esa copia
     * aunque la consulta traiga la fila nueva.
     */
    public void refresh() {
        getEntityManager().clear();
        updateAndSearch();
    }

    @Override
    public void clear() {
        employeeName = null;
        duration = null;
        activeForPayroll = null;
        contractStateName = defaultStateName();
        validOn = null;
        onlyExpiring = false;
        expiringDays = 30;
        super.clear();
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public ContractDuration getDuration() {
        return duration;
    }

    public void setDuration(ContractDuration duration) {
        this.duration = duration;
    }

    public Boolean getActiveForPayroll() {
        return activeForPayroll;
    }

    public void setActiveForPayroll(Boolean activeForPayroll) {
        this.activeForPayroll = activeForPayroll;
    }

    public String getContractStateName() {
        return contractStateName;
    }

    /**
     * La opcion en blanco del combo llega como cadena vacia, no como null, y una restriccion
     * con cadena vacia no se apaga: filtraria por un estado que no existe y no devolveria nada.
     */
    public void setContractStateName(String contractStateName) {
        this.contractStateName = (null == contractStateName || 0 == contractStateName.trim().length())
                ? null : contractStateName.trim();
    }

    public Date getValidOn() {
        return validOn;
    }

    public void setValidOn(Date validOn) {
        this.validOn = validOn;
    }

    public boolean isOnlyExpiring() {
        return onlyExpiring;
    }

    public void setOnlyExpiring(boolean onlyExpiring) {
        this.onlyExpiring = onlyExpiring;
    }

    public int getExpiringDays() {
        return expiringDays;
    }

    public void setExpiringDays(int expiringDays) {
        this.expiringDays = expiringDays;
    }

    public ContractDuration[] getDurations() {
        return ContractDuration.values();
    }
}
