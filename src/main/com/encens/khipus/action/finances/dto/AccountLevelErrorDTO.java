package com.encens.khipus.action.finances.dto;

/**
 * Fila de error detectada al analizar los niveles del plan de cuentas
 * (tabla arcgms) desde la pantalla "Cuentas contables".
 *
 * Se usa para tres tipos de error, cada uno mostrado en su propia seccion
 * del modal de analisis:
 *  - FORMATO: la cuenta no tiene 10 digitos (no se procesa, solo se informa).
 *             En este caso currentValue lleva la longitud real de la cuenta.
 *  - RAIZ   : cta_raiz distinta de los 2 primeros digitos + 8 ceros.
 *  - NIVEL 3: cta_niv3 distinta de lo esperado segun el nivel de la cuenta
 *             (vacio en niveles 1-2, 3 primeros digitos + 7 ceros en 3+).
 *
 * currentValue = valor actual en BD; correctValue = valor esperado.
 */
public class AccountLevelErrorDTO {

    private String companyNumber;
    private String accountCode;
    private String description;
    private Integer accountLevel;
    private String currentValue;
    private String correctValue;

    /**
     * Indica si la cuenta calculada en correctValue existe en arcgms (misma
     * compania). Si es false, la fila NO se corrige (se marca en rojo y queda
     * hasta que se cree/corrija la cuenta destino). Para errores de formato y
     * para el valor vacio de niveles 1-2 siempre es true (no aplica destino).
     */
    private boolean targetExists = true;

    public AccountLevelErrorDTO() {
    }

    public AccountLevelErrorDTO(String companyNumber, String accountCode, String description,
                                Integer accountLevel, String currentValue, String correctValue) {
        this(companyNumber, accountCode, description, accountLevel, currentValue, correctValue, true);
    }

    public AccountLevelErrorDTO(String companyNumber, String accountCode, String description,
                                Integer accountLevel, String currentValue, String correctValue,
                                boolean targetExists) {
        this.companyNumber = companyNumber;
        this.accountCode = accountCode;
        this.description = description;
        this.accountLevel = accountLevel;
        this.currentValue = currentValue;
        this.correctValue = correctValue;
        this.targetExists = targetExists;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAccountLevel() {
        return accountLevel;
    }

    public void setAccountLevel(Integer accountLevel) {
        this.accountLevel = accountLevel;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getCorrectValue() {
        return correctValue;
    }

    public void setCorrectValue(String correctValue) {
        this.correctValue = correctValue;
    }

    public boolean isTargetExists() {
        return targetExists;
    }

    public void setTargetExists(boolean targetExists) {
        this.targetExists = targetExists;
    }
}
