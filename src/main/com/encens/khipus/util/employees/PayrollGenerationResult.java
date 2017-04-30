package com.encens.khipus.util.employees;

/**
 * PayrollGenerationResult
 *
 * @author
 * @version 1.4.1
 */
public enum PayrollGenerationResult {
    SUCCESS, FAIL, WITHOUT_CONTRACTS, WITHOUT_BANDS;
    private Object[] resultData;

    public Object[] getResultData() {
        return resultData;
    }

    public PayrollGenerationResult assignResultData(Object... resultData) {
        this.resultData = resultData;
        return this;
    }
}
