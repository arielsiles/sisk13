package com.encens.khipus.util.employees;

/**
 * PayrollGenerationResult
 *
 * @author
 * @version 1.4.1
 */
public enum PayrollGenerationResult {
    SUCCESS, FAIL, WITHOUT_CONTRACTS, WITHOUT_BANDS,
    /** Tiene contratos en el periodo pero ninguno marcado como principal: RRHH tiene que elegir. */
    WITHOUT_MAIN_CONTRACT,
    /** Mas de un contrato principal a la vez: contradice la invariante, son datos rotos. */
    SEVERAL_MAIN_CONTRACTS,
    /** Los dias a pagar no cierran contra los dias de contrato. Se rechaza en vez de recortar. */
    INCONSISTENT_DAYS,
    /** Se le controla la asistencia pero su codigo de marcado no se puede leer. */
    WITHOUT_MARK_CODE;
    private Object[] resultData;

    public Object[] getResultData() {
        return resultData;
    }

    public PayrollGenerationResult assignResultData(Object... resultData) {
        this.resultData = resultData;
        return this;
    }
}
