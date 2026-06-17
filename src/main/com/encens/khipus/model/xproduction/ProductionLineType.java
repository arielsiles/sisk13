package com.encens.khipus.model.xproduction;

/**
 * Tipo de linea de produccion (plantilla especializada).
 *
 * Generaliza el discriminador {@code report_template_code} de {@link ProductionLine}:
 * cada valor representa una plantilla especializada que activa campos/paneles
 * propios en la orden de produccion (xpr_produccion).
 *
 * La ausencia de tipo (codigo nulo) corresponde a una linea GENERAL, que solo
 * usa el flujo comun (Formulacion, Insumos, Materiales, Productos Terminados).
 *
 * Para agregar una nueva linea especializada a futuro basta con declarar un
 * nuevo valor aqui (codigo + resourceKey) y su panel/tabla satelite; no se
 * hardcodean cadenas en acciones ni vistas.
 */
public enum ProductionLineType {

    ULEXITA("ULEXITA", "ProductionLine.type.ulexita"),
    BARITINA("BARITINA", "ProductionLine.type.baritina");

    private final String code;
    private final String resourceKey;

    ProductionLineType(String code, String resourceKey) {
        this.code = code;
        this.resourceKey = resourceKey;
    }

    public String getCode() {
        return code;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    /**
     * Resuelve el tipo a partir del codigo persistido en report_template_code.
     * Retorna {@code null} (linea GENERAL) si el codigo es nulo o desconocido.
     */
    public static ProductionLineType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ProductionLineType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
