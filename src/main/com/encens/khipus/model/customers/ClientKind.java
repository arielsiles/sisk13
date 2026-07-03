package com.encens.khipus.model.customers;

/**
 * Clase de cliente: persona natural o institucion (persona juridica).
 * <p/>
 * Equivalente al company_type de Odoo: es una vista tipada de la unica fuente
 * de verdad {@code espersona} (personFlag). El valor persistido en la columna
 * {@code clase_cliente} se deriva de aqui (ver Client#syncClientKind), nunca se
 * escribe con literales sueltos.
 */
public enum ClientKind {

    PERSON("persona"),
    INSTITUTION("institucion");

    private final String value;

    ClientKind(String value) {
        this.value = value;
    }

    /** Valor persistido en la columna clase_cliente. */
    public String getValue() {
        return value;
    }
}
