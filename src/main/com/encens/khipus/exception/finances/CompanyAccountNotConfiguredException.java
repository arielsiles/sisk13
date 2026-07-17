package com.encens.khipus.exception.finances;

/**
 * Se lanza cuando una cuenta contable de la configuracion de compania
 * (tabla <code>configuracion</code>) es requerida por un flujo pero no esta
 * configurada: la columna esta en NULL, o apunta a una cuenta que ya no existe
 * en el plan de cuentas.
 * <p>
 * Antes de esto, esos dos casos se manifestaban como un NullPointerException o
 * como un EntityNotFoundException de Hibernate salido de un proxy lazy: ninguno
 * de los dos decia QUE cuenta faltaba, asi que no habia forma de saber que ir a
 * configurar.
 * <p>
 * Es unchecked a proposito: los ~60 puntos que consumen estas cuentas no tienen
 * como recuperarse: la respuesta siempre es ir a configurarla. La maneja el
 * <code>&lt;exception&gt;</code> declarado en WEB-INF/pages.xml, que arma el
 * texto con {@link CompanyAccountNotConfiguredMessage}.
 *
 * @author
 * @version 1.0
 */
public class CompanyAccountNotConfiguredException extends RuntimeException {

    /**
     * Nombre fisico de la columna en la tabla configuracion (ej. "ctacostpv").
     */
    private final String columnName;

    /**
     * Clave i18n de la etiqueta del campo (ej. "CompanyConfiguration.ctaCostPV").
     */
    private final String labelKey;

    public CompanyAccountNotConfiguredException(String columnName, String labelKey) {
        super("La cuenta contable de la columna '" + columnName + "' no esta configurada en Preferencias de compania");
        this.columnName = columnName;
        this.labelKey = labelKey;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getLabelKey() {
        return labelKey;
    }
}
