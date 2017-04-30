package com.encens.khipus.exception.production;


import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ProductCompositionException extends Exception {

    public static final String TOPOLOGICAL_SORTING = "TOPOLOGICAL_SORTING";
    public static final String NO_FOUND_VARIABLE = "NO_FOUND_VARIABLE";

    private String data;
    private String code;
    private boolean isCyclic;

    public ProductCompositionException() {

    }

    public ProductCompositionException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public ProductCompositionException(String code, boolean isCyclick) {
        this.code = code;
        this.isCyclic = isCyclick;
    }

    public ProductCompositionException(String code, String data, Throwable cause) {
        super(cause);
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public boolean isCyclic() {
        return isCyclic;
    }

    public String getData() {
        return data;
    }
}
