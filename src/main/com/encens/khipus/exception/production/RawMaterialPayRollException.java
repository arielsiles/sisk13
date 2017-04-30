package com.encens.khipus.exception.production;


import java.util.Date;

public class RawMaterialPayRollException extends Exception {

    public static final String CROSS_WITH_ANOTHER_PAYROLL = "CROSS_WITH_ANOTHER_PAYROLL";
    public static final String MINIMUM_START_DATE = "MINIMUM_START_DATE";
    public static final String NO_COLLECTION_ON_DATE = "NO_COLLECTION_ON_DATE";

    private String code;
    private Date date;

    public RawMaterialPayRollException(String code, Date date) {
        super();
        this.code = code;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public Date getDate() {
        return date;
    }
}
