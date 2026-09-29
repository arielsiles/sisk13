package com.encens.khipus.util.employees.payroll;

/**
 * Sobre que base se valoriza el dia al descontar un atraso.
 * <p/>
 * Las dos existen porque cada empresa lo tiene escrito distinto en su reglamento interno, y el
 * numero cambia: con bono de antiguedad, el total ganado es mayor que el sueldo, asi que el mismo
 * atraso le cuesta mas a quien tiene mas anios en la empresa. La norma boliviana habla del
 * <i>haber</i>, que es el sueldo; la practica de varios clientes usa el total ganado. Lo decide la
 * empresa en sus preferencias, no el codigo.
 *
 * @author
 * @version 6.1.0
 */
public enum LatenessDiscountBase {

    /** Basico ganado + bono de antiguedad + otros ingresos. Es lo que se venia haciendo. */
    TOTAL_INCOME("LatenessDiscountBase.totalIncome"),

    /** El sueldo del contrato, el del mes completo, sin prorratear ni sumar bonos. */
    BASIC_SALARY("LatenessDiscountBase.basicSalary");

    private final String resourceKey;

    LatenessDiscountBase(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    /** Lo que se aplica si la empresa no eligio: lo de siempre, para no cambiarle el numero a nadie. */
    public static LatenessDiscountBase orDefault(LatenessDiscountBase base) {
        return null == base ? TOTAL_INCOME : base;
    }
}
