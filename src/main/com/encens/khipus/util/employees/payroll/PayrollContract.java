package com.encens.khipus.util.employees.payroll;

import com.encens.khipus.model.finances.Contract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cual de los contratos de una persona es el que va a la planilla.
 * <p/>
 * La respuesta anterior era `contratos.get(0)`, y no era siquiera un contrato al azar: la consulta
 * ordena por fecha de inicio, asi que devolvia sistematicamente el <b>mas viejo</b> de los que
 * tocan el mes. En una recontratacion, el que ya termino. De ahi salia el sueldo.
 * <p/>
 * <b>La regla es una sola: a la planilla va el contrato principal.</b> De el salen el sueldo, el
 * puesto, el centro de costo y las cuentas; sobre el se controla la asistencia; y sus fechas dan
 * los dias. Los secundarios no entran: son trabajo eventual con fecha de inicio y fin, no llevan
 * AFP ni vacaciones, y tampoco generan faltas contra el sueldo mensual.
 * <p/>
 * Si no hay exactamente uno, <b>no se elige</b>. Que RRHH designe el principal es una decision
 * suya -es lo que dice el plan 08- y adivinar cual de tres sueldos cobra alguien es peor que
 * frenar la generacion y pedirlo.
 * <p/>
 * Se le pasa la lista que la planilla ya tiene, que viene filtrada por unidad de negocio,
 * categoria, `activogenplan` y solape con el periodo. Aca solo se elige.
 *
 * @author
 * @version 6.1.0
 */
public final class PayrollContract {

    public enum Result {
        /** Hay exactamente un principal: se paga por el. */
        SELECTED,
        /** Hay contratos en el periodo pero ninguno es principal. RRHH tiene que designarlo. */
        WITHOUT_MAIN,
        /** Mas de un principal: contradice la invariante y son datos rotos. */
        SEVERAL_MAIN,
        /** No hay ningun contrato en el periodo. */
        WITHOUT_CONTRACT
    }

    private final Result result;
    private final Contract contract;
    private final int candidates;

    private PayrollContract(Result result, Contract contract, int candidates) {
        this.result = result;
        this.contract = contract;
        this.candidates = candidates;
    }

    public static PayrollContract select(List<Contract> contracts) {
        if (null == contracts || contracts.isEmpty()) {
            return new PayrollContract(Result.WITHOUT_CONTRACT, null, 0);
        }
        List<Contract> main = new ArrayList<Contract>();
        for (Contract contract : contracts) {
            if (null != contract && contract.isMain()) {
                main.add(contract);
            }
        }
        if (main.isEmpty()) {
            return new PayrollContract(Result.WITHOUT_MAIN, null, contracts.size());
        }
        if (main.size() > 1) {
            return new PayrollContract(Result.SEVERAL_MAIN, null, main.size());
        }
        return new PayrollContract(Result.SELECTED, main.get(0), 1);
    }

    public boolean isSelected() {
        return Result.SELECTED.equals(result);
    }

    public Result getResult() {
        return result;
    }

    public Contract getContract() {
        return contract;
    }

    /** Cuantos contratos habia para elegir. Sirve para explicar por que no se pudo. */
    public int getCandidates() {
        return candidates;
    }

    /**
     * El periodo del mes que cubre el contrato elegido, acotado al periodo de la planilla.
     * <p/>
     * Se mira por <b>fechas y no por estado</b>: un contrato dado de baja el 15 sigue pagando del
     * 1 al 15. Esta INACTIVO hoy, pero esos dias se trabajaron. El estado es una foto del
     * presente; las fechas son el hecho.
     */
    public Date getInitDate() {
        return null == contract ? null : contract.getInitDate();
    }

    public Date getEndDate() {
        return null == contract ? null : contract.getEndDate();
    }
}
