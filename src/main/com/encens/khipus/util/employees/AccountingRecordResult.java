package com.encens.khipus.util.employees;

/**
 * AccountingRecordResult
 *
 * @author
 * @version 1.4
 */
public enum AccountingRecordResult {
    SUCCESS, FAIL, WITHOUT_OFFICIAL_GENERATION, WITHOUT_COSTCENTER;
    private Object[] resultData;

    public Object[] getResultData() {
        return resultData;
    }

    public AccountingRecordResult assignResultData(Object... resultData) {
        this.resultData = resultData;
        return this;
    }
}
