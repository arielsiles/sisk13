package com.encens.khipus.util.employees.payroll;

import com.encens.khipus.util.employees.PayrollGenerationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Las personas que impiden generar la planilla, agrupadas por causa.
 * <p/>
 * Se acumulan en vez de cortar en la primera: asi RRHH las corrige de una sola pasada en lugar de
 * generar, fallar, arreglar una y volver a empezar. La corrida se descarta igual al final -no se
 * genera una planilla a medias-.
 * <p/>
 * Estaba resuelto con dos listas sueltas pasadas como parametros. Cada causa nueva agregaba otro
 * parametro y otra rama en el mismo `if`, que es como se llega a una firma de quince argumentos.
 *
 * @author
 * @version 6.1.0
 */
public class PayrollBlockers {

    private final List<String> withoutContracts = new ArrayList<String>();
    private final List<String> withoutBands = new ArrayList<String>();
    private final List<String> withoutMainContract = new ArrayList<String>();
    private final List<String> severalMainContracts = new ArrayList<String>();
    private final List<String> inconsistentDays = new ArrayList<String>();
    private final List<String> withoutMarkCode = new ArrayList<String>();

    public void addWithoutContracts(String employee) {
        withoutContracts.add(employee);
    }

    public void addWithoutBands(String employee) {
        withoutBands.add(employee);
    }

    public void addWithoutMainContract(String employee) {
        withoutMainContract.add(employee);
    }

    public void addSeveralMainContracts(String employee) {
        severalMainContracts.add(employee);
    }

    public void addInconsistentDays(String employee) {
        inconsistentDays.add(employee);
    }

    public void addWithoutMarkCode(String employee) {
        withoutMarkCode.add(employee);
    }

    public boolean isEmpty() {
        return withoutContracts.isEmpty() && withoutBands.isEmpty()
                && withoutMainContract.isEmpty() && severalMainContracts.isEmpty()
                && inconsistentDays.isEmpty() && withoutMarkCode.isEmpty();
    }

    /**
     * El resultado a devolver, o null si no hay nada que impida generar.
     * <p/>
     * El orden es el de la causa raiz: quien no tiene contrato tampoco va a tener principal, asi
     * que reportarle las dos cosas seria ruido. Se le muestra la que tiene que resolver primero.
     */
    public PayrollGenerationResult evaluate(Joiner joiner) {
        if (!withoutContracts.isEmpty()) {
            return PayrollGenerationResult.WITHOUT_CONTRACTS.assignResultData(joiner.join(withoutContracts));
        }
        if (!severalMainContracts.isEmpty()) {
            return PayrollGenerationResult.SEVERAL_MAIN_CONTRACTS.assignResultData(joiner.join(severalMainContracts));
        }
        if (!withoutMainContract.isEmpty()) {
            return PayrollGenerationResult.WITHOUT_MAIN_CONTRACT.assignResultData(joiner.join(withoutMainContract));
        }
        if (!withoutBands.isEmpty()) {
            return PayrollGenerationResult.WITHOUT_BANDS.assignResultData(joiner.join(withoutBands));
        }
        if (!withoutMarkCode.isEmpty()) {
            return PayrollGenerationResult.WITHOUT_MARK_CODE.assignResultData(joiner.join(withoutMarkCode));
        }
        if (!inconsistentDays.isEmpty()) {
            return PayrollGenerationResult.INCONSISTENT_DAYS.assignResultData(joiner.join(inconsistentDays));
        }
        return null;
    }

    /** Como se arma el texto de la lista. Lo decide quien llama, que sabe de idioma y formato. */
    public interface Joiner {
        String join(List<String> names);
    }
}
