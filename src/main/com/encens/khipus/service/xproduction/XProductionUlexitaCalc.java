package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.ProductionShiftType;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.model.xproduction.XProductionUlexita;
import com.encens.khipus.model.xproduction.XSupply;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Capa de calculo para una orden de produccion de la linea ULEXITA.
 *
 * Centraliza todas las formulas del reporte diario de produccion ULEXITA:
 *   - derivados directos (PT total bueno, diluyente, % bentonita / caolin)
 *   - indicadores de proceso (Kpa, Kpm bentonita, Kpm merma)
 *   - merma absoluta y % merma
 *   - leyes recalculadas y consumo de MP teorico
 *
 * Cualquier division por cero o input requerido nulo retorna null en el
 * getter respectivo. La UI y el reporte interpretan null como celda vacia.
 */
public class XProductionUlexitaCalc {

    public static final int SCALE = 6;
    public static final int PCT_SCALE = 4;
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    public static final String UNIT_KG = "KG";

    private final XProduction production;
    private final XProductionUlexita ulexita;
    private final ProductionLine line;
    private final List<XSupply> supplies;
    private final List<XProductionProduct> products;
    /**
     * Si true, ignora snapshots y siempre calcula en vivo. Usado durante la
     * generacion misma del snapshot (persistSnapshots) para no leer valores
     * potencialmente desactualizados.
     */
    private final boolean forceLive;

    public XProductionUlexitaCalc(XProduction production,
                                  XProductionUlexita ulexita,
                                  ProductionLine line,
                                  List<XSupply> supplies,
                                  List<XProductionProduct> products) {
        this(production, ulexita, line, supplies, products, false);
    }

    public XProductionUlexitaCalc(XProduction production,
                                  XProductionUlexita ulexita,
                                  ProductionLine line,
                                  List<XSupply> supplies,
                                  List<XProductionProduct> products,
                                  boolean forceLive) {
        this.production = production;
        this.ulexita = ulexita;
        this.line = line;
        this.supplies = supplies != null ? supplies : Collections.<XSupply>emptyList();
        this.products = products != null ? products : Collections.<XProductionProduct>emptyList();
        this.forceLive = forceLive;
    }

    /**
     * True si los getters deben leer snapshots persistidos (orden aprobada con snap completado).
     * False si deben recalcular en vivo (pendiente, sin snap, o forceLive=true).
     */
    private boolean useSnapshots() {
        if (forceLive) return false;
        if (ulexita == null || !ulexita.hasSnapshots()) return false;
        return production != null && production.isApproved();
    }

    // ------------------------------------------------------------------ derivados directos

    /** Letra del dia de la semana segun initDate ("L","M","M","J","V","S","D"). */
    public String getDia() {
        if (production == null || production.getInitDate() == null) return "";
        Calendar c = Calendar.getInstance();
        c.setTime(production.getInitDate());
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:    return "L";
            case Calendar.TUESDAY:   return "M";
            case Calendar.WEDNESDAY: return "M";
            case Calendar.THURSDAY:  return "J";
            case Calendar.FRIDAY:    return "V";
            case Calendar.SATURDAY:  return "S";
            case Calendar.SUNDAY:    return "D";
            default: return "";
        }
    }

    /** Suma de cantidades de los insumos cuyo cod_art = linea.codArtMpPrincipal, convertida a TN. */
    public BigDecimal getConsumoMpReal() {
        return sumSupplyQuantityInTn(line != null ? line.getCodArtMpPrincipal() : null);
    }

    /**
     * Diluyente total (TN). Override manual u.diluyenteTotalTn si esta poblado;
     * caso contrario suma insumos diluyentes (bentonita + caolin) y convierte KG a TN.
     */
    public BigDecimal getDiluyenteTotal() {
        if (useSnapshots()) return ulexita.getDiluyenteTotalSnap();
        if (ulexita != null && ulexita.getDiluyenteTotalTn() != null) {
            return ulexita.getDiluyenteTotalTn();
        }
        if (line == null) return null;
        BigDecimal bent = sumSupplyQuantityInTn(line.getCodArtDiluyBent());
        BigDecimal caol = sumSupplyQuantityInTn(line.getCodArtDiluyCaolin());
        BigDecimal total = nullToZero(bent).add(nullToZero(caol));
        return total.signum() == 0 ? null : total;
    }

    /** Cantidad de bentonita usada en la produccion (TN). */
    public BigDecimal getBentonitaQty() {
        return line != null ? sumSupplyQuantityInTn(line.getCodArtDiluyBent()) : null;
    }

    /**
     * Proporcion bentonita (%). Override u.bentonitaPct si esta poblado;
     * caso contrario derivada como bentonita_qty / diluyente_total * 100.
     */
    public BigDecimal getBentonitaPct() {
        if (useSnapshots()) return ulexita.getBentonitaPctSnap();
        if (ulexita != null && ulexita.getBentonitaPct() != null) {
            return ulexita.getBentonitaPct();
        }
        BigDecimal bent = getBentonitaQty();
        BigDecimal dil = getDiluyenteTotal();
        if (bent == null || !isPositive(dil)) return null;
        return bent.divide(dil, PCT_SCALE, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
    }

    /** Proporcion caolin (%) = 100 - bentonita%. */
    public BigDecimal getCaolinPct() {
        if (useSnapshots()) return ulexita.getCaolinPctSnap();
        BigDecimal bent = getBentonitaPct();
        return bent == null ? null : ONE_HUNDRED.subtract(bent);
    }

    /** Cantidad de PT clasificacion A (suma productos con cod_art = linea.codArtPtA, en TN). */
    public BigDecimal getPtA() {
        if (useSnapshots()) return ulexita.getPtASnap();
        return sumProductQuantityInTn(line != null ? line.getCodArtPtA() : null);
    }

    /** Cantidad de PT clasificacion B (suma productos con cod_art = linea.codArtPtB, en TN). */
    public BigDecimal getPtB() {
        if (useSnapshots()) return ulexita.getPtBSnap();
        return sumProductQuantityInTn(line != null ? line.getCodArtPtB() : null);
    }

    /** PT total bueno = A + B. */
    public BigDecimal getPtTotalBueno() {
        if (useSnapshots()) return ulexita.getPtTotalBuenoSnap();
        BigDecimal a = nullToZero(getPtA());
        BigDecimal b = nullToZero(getPtB());
        BigDecimal r = a.add(b);
        return r.signum() == 0 ? null : r;
    }

    // ------------------------------------------------------------------ indicadores

    /** Kpa = PRODUCTO GRANULADO / PT_TOTAL_BUENO. */
    public BigDecimal getKpa() {
        if (useSnapshots()) return ulexita.getKpaSnap();
        BigDecimal granulado = ulexita != null ? ulexita.getProductoGranuladoTn() : null;
        BigDecimal pt = getPtTotalBueno();
        if (granulado == null || !isPositive(pt)) return null;
        return granulado.divide(pt, SCALE, RoundingMode.HALF_UP);
    }

    /** Kpm bentonita = Ley_PT / Ley_MP_con_bentonita. */
    public BigDecimal getKpmBentonita() {
        if (useSnapshots()) return ulexita.getKpmBentonitaSnap();
        if (ulexita == null) return null;
        BigDecimal num = ulexita.getLeyPt();
        BigDecimal den = ulexita.getLeyMpBentonita();
        if (num == null || !isPositive(den)) return null;
        return num.divide(den, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Kpm merma = (Kpm_bentonita - 1) - Diluyente / (PT_TOTAL_BUENO * mermaFactor * Kpa) + 1
     * Equivalente a Excel col X.
     */
    public BigDecimal getKpmMerma() {
        if (useSnapshots()) return ulexita.getKpmMermaSnap();
        BigDecimal kpmBent = getKpmBentonita();
        BigDecimal dil = getDiluyenteTotal();
        BigDecimal pt = getPtTotalBueno();
        BigDecimal kpa = getKpa();
        BigDecimal mermaFactor = getMermaFactor();
        // El factor va en el divisor: sin configurar, cero o negativo, el calculo no existe.
        if (kpmBent == null || dil == null || !isPositive(pt) || !isPositive(kpa)
                || !isPositive(mermaFactor)) return null;
        BigDecimal denom = pt.multiply(mermaFactor).multiply(kpa);
        if (!isPositive(denom)) return null;
        BigDecimal frac = dil.divide(denom, SCALE, RoundingMode.HALF_UP);
        return kpmBent.subtract(BigDecimal.ONE).subtract(frac).add(BigDecimal.ONE);
    }

    /** MERMA (TN) = (Kpm_merma - 1) * mermaFactor * Kpa * PT_TOTAL_BUENO. Excel col Z. */
    public BigDecimal getMerma() {
        if (useSnapshots()) return ulexita.getMermaSnap();
        BigDecimal kpmMerma = getKpmMerma();
        BigDecimal kpa = getKpa();
        BigDecimal pt = getPtTotalBueno();
        if (kpmMerma == null || kpa == null || pt == null) return null;
        return kpmMerma.subtract(BigDecimal.ONE).multiply(getMermaFactor()).multiply(kpa).multiply(pt)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** % MERMA = MERMA / (MERMA + GRANULADO). Excel col AA. */
    public BigDecimal getMermaPct() {
        if (useSnapshots()) return ulexita.getMermaPctSnap();
        BigDecimal merma = getMerma();
        BigDecimal granulado = ulexita != null ? ulexita.getProductoGranuladoTn() : null;
        if (merma == null || granulado == null) return null;
        BigDecimal denom = merma.add(granulado);
        if (!isPositive(denom)) return null;
        return merma.divide(denom, SCALE, RoundingMode.HALF_UP);
    }

    /** Ley MP recalculada = Ley_PT / Kpm_merma. Excel col M. */
    public BigDecimal getLeyMpRecalc() {
        if (useSnapshots()) return ulexita.getLeyMpRecalcSnap();
        if (ulexita == null) return null;
        BigDecimal num = ulexita.getLeyPt();
        BigDecimal den = getKpmMerma();
        if (num == null || !isPositive(den)) return null;
        return num.divide(den, SCALE, RoundingMode.HALF_UP);
    }

    /** Consumo MP teorico = MERMA + Kpa * PT_TOTAL_BUENO - CONSUMO_REPROCESO. Excel col E. */
    public BigDecimal getConsumoMpCalc() {
        if (useSnapshots()) return ulexita.getConsumoMpCalcSnap();
        BigDecimal merma = getMerma();
        BigDecimal kpa = getKpa();
        BigDecimal pt = getPtTotalBueno();
        if (merma == null || kpa == null || pt == null) return null;
        BigDecimal reproceso = ulexita != null ? ulexita.getConsumoReprocesoTn() : null;
        return merma.add(kpa.multiply(pt)).subtract(nullToZero(reproceso)).setScale(SCALE, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ getters auxiliares

    /**
     * Factor de merma configurado en la linea (o el snapshot, si la orden esta aprobada).
     *
     * No hay valor por defecto: el factor lo define el usuario. Si no esta configurado, las
     * formulas que dependen de el (Kpm merma y MERMA) devuelven null y el reporte muestra celda
     * vacia, igual que con cualquier otro dato faltante. Sustituirlo por una constante ocultaria
     * que la linea esta mal configurada y falsearia el calculo.
     */
    public BigDecimal getMermaFactor() {
        if (useSnapshots() && ulexita.getMermaFactorSnap() != null) {
            return ulexita.getMermaFactorSnap();
        }
        return line != null ? line.getMermaFactor() : null;
    }

    public boolean isShiftDay() {
        return production != null && ProductionShiftType.D.equals(production.getProductionShiftType());
    }

    public boolean isShiftNight() {
        return production != null && ProductionShiftType.N.equals(production.getProductionShiftType());
    }

    public XProduction getProduction() {
        return production;
    }

    public XProductionUlexita getUlexita() {
        return ulexita;
    }

    // ------------------------------------------------------------------ helpers privados

    /**
     * Suma cantidades de insumos con cod_art dado y convierte a TN segun la
     * unidad de medida del articulo (KG -> /1000, TN -> sin cambio).
     */
    private BigDecimal sumSupplyQuantityInTn(String codArt) {
        if (codArt == null) return null;
        BigDecimal total = BigDecimal.ZERO;
        String unit = null;
        boolean found = false;
        for (XSupply s : supplies) {
            if (codArt.equals(s.getProductItemCode()) && s.getQuantity() != null) {
                total = total.add(s.getQuantity());
                if (unit == null && s.getProductItem() != null) {
                    unit = s.getProductItem().getUsageMeasureCode();
                }
                found = true;
            }
        }
        return found ? convertToTn(total, unit) : null;
    }

    /**
     * Suma cantidades de productos terminados con cod_art dado y convierte a TN
     * segun la unidad de medida del articulo.
     */
    private BigDecimal sumProductQuantityInTn(String codArt) {
        if (codArt == null) return null;
        BigDecimal total = BigDecimal.ZERO;
        String unit = null;
        boolean found = false;
        for (XProductionProduct p : products) {
            if (codArt.equals(p.getProductItemCode()) && p.getQuantity() != null) {
                total = total.add(p.getQuantity());
                if (unit == null && p.getProductItem() != null) {
                    unit = p.getProductItem().getUsageMeasureCode();
                }
                found = true;
            }
        }
        return found ? convertToTn(total, unit) : null;
    }

    /** Convierte un valor a TN segun unidad. KG -> /1000. TN o desconocido -> sin cambio. */
    private static BigDecimal convertToTn(BigDecimal value, String unit) {
        if (value == null) return null;
        if (UNIT_KG.equalsIgnoreCase(unit)) {
            return value.divide(ONE_THOUSAND, SCALE, RoundingMode.HALF_UP);
        }
        return value;
    }

    private static boolean isPositive(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
