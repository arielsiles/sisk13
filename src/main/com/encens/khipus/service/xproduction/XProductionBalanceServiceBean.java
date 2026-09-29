package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.xproduction.ProductionLineType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemState;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehouseState;
import com.encens.khipus.model.warehouse.WarehouseType;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@Name("xproductionBalanceService")
@AutoCreate
public class XProductionBalanceServiceBean implements XProductionBalanceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    /**
     * Filtro "hasta la fecha" para las fuentes de produccion (4/5/6): usa la fecha del
     * plan de produccion ({@code productionPlan.date}). Las ordenes sin plan (sin fecha)
     * se cuentan siempre, para no alterar los totales actuales. Requiere el alias
     * {@code pl} (plan) y el parametro {@code :cutoff}.
     */
    private static final String PRODUCTION_DATE_FILTER =
            "(pl.date is null or pl.date <= :cutoff) ";

    @Override
    @SuppressWarnings("unchecked")
    public List<Warehouse> findBalanceWarehouses() {
        return em.createQuery(
                "select w from Warehouse w " +
                "where w.warehouseType in (:types) and w.state <> :blo " +
                "order by w.name")
                .setParameter("types", Arrays.asList(WarehouseType.RAW_MATERIAL, WarehouseType.FINISHED_GOODS))
                .setParameter("blo", WarehouseState.BLO)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WarehouseBalanceRow> computeBalances(String companyNumber, String warehouseCode, Date date) {
        Map<String, WarehouseBalanceRow> rows = new LinkedHashMap<String, WarehouseBalanceRow>();
        if (companyNumber == null || warehouseCode == null) {
            return new ArrayList<WarehouseBalanceRow>();
        }

        // Corte "hasta la fecha": se normaliza al fin del dia seleccionado para incluir
        // tanto fechas guardadas a medianoche (DATE) como con hora (TIMESTAMP).
        Date cutoff = endOfDay(date != null ? date : new Date());

        // 1) Todos los productos del almacen (incluso saldo 0), ordenados por
        //    Subgrupo y luego por nombre (A-Z). LEFT JOIN para incluir articulos
        //    sin subgrupo.
        List<Object[]> products = em.createQuery(
                "select p, sg from ProductItem p left join p.subGroup sg " +
                "where p.warehouseCode = :wc and p.companyNumber = :cn and p.state = :vig " +
                "order by sg.name, p.name")
                .setParameter("wc", warehouseCode)
                .setParameter("cn", companyNumber)
                .setParameter("vig", ProductItemState.VIG)
                .getResultList();
        for (Object[] r : products) {
            ProductItem p = (ProductItem) r[0];
            SubGroup sg = (SubGroup) r[1];
            String sgCode = sg != null ? sg.getSubGroupCode() : "";
            String sgName = sg != null ? sg.getName() : "(Sin subgrupo)";
            rows.put(p.getProductItemCode(),
                    new WarehouseBalanceRow(p.getProductItemCode(), p.getName(),
                            p.getUsageMeasureCode(), sgCode, sgName, BigDecimal.ZERO));
        }

        // 2) Kardex inv_movdet (vales de entrada/salida + despachos), por tipo E/S. Solo vales
        //    APROBADOS (mismo criterio que el reporte de movimientos por articulo).
        List<Object[]> movements = em.createQuery(
                "select md.productItemCode, md.movementType, sum(md.quantity) from MovementDetail md " +
                "where md.companyNumber = :cn and md.warehouseCode = :wc and md.state = :apr " +
                "and md.movementDetailDate <= :cutoff " +
                "group by md.productItemCode, md.movementType")
                .setParameter("cn", companyNumber)
                .setParameter("wc", warehouseCode)
                .setParameter("apr", WarehouseVoucherState.APR)
                .setParameter("cutoff", cutoff)
                .getResultList();
        for (Object[] r : movements) {
            WarehouseBalanceRow row = rows.get((String) r[0]);
            if (row == null) {
                continue;
            }
            if (MovementDetailType.E.equals(r[1])) {
                row.add((BigDecimal) r[2]);
            } else if (MovementDetailType.S.equals(r[1])) {
                row.subtract((BigDecimal) r[2]);
            }
        }

        // 3) Acopio de Materia Prima (entrada). Se usa el Peso Empresa (pesobal / balanceWeight)
        //    y solo acopios Aprobados/Contabilizados (mismo criterio que el reporte de movimientos).
        List<Object[]> collect = em.createQuery(
                "select cm.metaProduct.productItemCode, sum(cm.balanceWeight) from CollectMaterial cm " +
                "where cm.state in (:apr, :conta) and cm.date <= :cutoff " +
                "group by cm.metaProduct.productItemCode")
                .setParameter("apr", CollectMaterialState.APR)
                .setParameter("conta", CollectMaterialState.CONTA)
                .setParameter("cutoff", cutoff)
                .getResultList();
        applySums(rows, collect, true);

        // 4) Produccion: Producto Terminado producido (entrada). Orden != ANL y con
        //    fecha del plan de produccion <= corte.
        List<Object[]> produced = em.createQuery(
                "select pp.productItemCode, sum(pp.quantity) from XProductionProduct pp " +
                "left join pp.production pr left join pr.productionPlan pl " +
                "where pr.state <> :anl and " + PRODUCTION_DATE_FILTER +
                "group by pp.productItemCode")
                .setParameter("anl", ProductionState.ANL)
                .setParameter("cutoff", cutoff)
                .getResultList();
        applySums(rows, produced, true);

        // 5) Produccion: consumo de insumos (salida). Incluye PT usado como insumo. Orden != ANL.
        List<Object[]> consumed = em.createQuery(
                "select s.productItemCode, sum(s.quantity) from XSupply s " +
                "left join s.production pr left join pr.productionPlan pl " +
                "where pr.state <> :anl and " + PRODUCTION_DATE_FILTER +
                "group by s.productItemCode")
                .setParameter("anl", ProductionState.ANL)
                .setParameter("cutoff", cutoff)
                .getResultList();
        applySums(rows, consumed, false);

        // 6) Reproceso ULEXITA (tabla satelite xpr_produccion_ulexita), atribuido al
        //    articulo configurado en la orden (cod_art_reproc_final):
        //      'Reproceso final'  -> ENTRADA (alimenta el inventario del articulo)
        //      'Consumo reproceso'-> SALIDA  (descuenta del mismo articulo)
        //    Valores en TN; se convierten a la unidad del articulo. Orden != ANL.
        List<Object[]> reproceso = em.createQuery(
                "select u.codArtReprocFinal, sum(u.reprocesoFinalTn), sum(u.consumoReprocesoTn) " +
                "from XProductionUlexita u " +
                "left join u.production pr left join pr.productionPlan pl " +
                "where u.codArtReprocFinal is not null and pr.state <> :anl and " + PRODUCTION_DATE_FILTER +
                "group by u.codArtReprocFinal")
                .setParameter("anl", ProductionState.ANL)
                .setParameter("cutoff", cutoff)
                .getResultList();
        for (Object[] r : reproceso) {
            WarehouseBalanceRow row = rows.get((String) r[0]);
            if (row == null) {
                continue;
            }
            row.add(tnToUnit((BigDecimal) r[1], row.getMeasureCode()));      // Reproceso final -> entrada
            row.subtract(tnToUnit((BigDecimal) r[2], row.getMeasureCode())); // Consumo reproceso -> salida
        }

        // 7) Desglose por zona productiva de la MP BARITINA. Es solo detalle: no toca el saldo
        //    de ninguna fila, lo reparte (lo que ninguna zona explica va a "Sin zona asignada").
        attachZoneBreakdown(rows, cutoff);

        return new ArrayList<WarehouseBalanceRow>(rows.values());
    }

    @Override
    public Map<Long, ZoneBalanceRow> computeZoneBalances(String productItemCode, Date date, Long excludeProductionId) {
        Map<String, Map<Long, ZoneBalanceRow>> byItem = queryZoneBalances(
                Collections.singletonList(productItemCode), endOfDay(date != null ? date : new Date()), excludeProductionId);
        Map<Long, ZoneBalanceRow> zones = byItem.get(productItemCode);
        return zones != null ? zones : new LinkedHashMap<Long, ZoneBalanceRow>();
    }

    /**
     * Carga en cada MP de las lineas BARITINA con zonas su saldo por zona, mas la fila
     * "Sin zona asignada" = saldo del articulo − Σ zonas, para que el desglose sume siempre
     * el saldo. Ahi caen los vales/ajustes (no llevan zona) y las ordenes sin distribucion.
     */
    @SuppressWarnings("unchecked")
    private void attachZoneBreakdown(Map<String, WarehouseBalanceRow> rows, Date cutoff) {
        List<String> tracked = em.createQuery(
                "select distinct s.productItemCode from XSupply s " +
                "join s.production pr join pr.productionLine ln join s.formulationInput fi " +
                "where ln.reportTemplateCode = :baritina and ln.usaZonas = :usaZonas and fi.inputDefault = :inputDefault")
                .setParameter("baritina", ProductionLineType.BARITINA.getCode())
                .setParameter("usaZonas", Boolean.TRUE)
                .setParameter("inputDefault", Boolean.TRUE)
                .getResultList();
        List<String> codes = new ArrayList<String>();
        for (String code : tracked) {
            if (rows.containsKey(code)) {
                codes.add(code);
            }
        }
        if (codes.isEmpty()) {
            return;
        }

        Map<String, Map<Long, ZoneBalanceRow>> byItem = queryZoneBalances(codes, cutoff, null);
        for (String code : codes) {
            WarehouseBalanceRow row = rows.get(code);
            Map<Long, ZoneBalanceRow> itemZones = byItem.get(code);
            List<ZoneBalanceRow> zones = itemZones != null
                    ? new ArrayList<ZoneBalanceRow>(itemZones.values())
                    : new ArrayList<ZoneBalanceRow>();
            BigDecimal assigned = BigDecimal.ZERO;
            for (ZoneBalanceRow zone : zones) {
                assigned = assigned.add(zone.getBalance());
            }
            zones.add(ZoneBalanceRow.unassigned(row.getBalance().subtract(assigned)));
            row.setZones(zones);
        }
    }

    /**
     * Saldo por (articulo, zona) con los mismos criterios que el saldo total:
     *   acopio  = Σ pesobal de acopios APR/CONTA de la zona, fecha &lt;= corte;
     *   consumo = Σ (insumo MP por defecto × porcentaje / 100) de las ordenes != ANL que
     *             distribuyeron por zona, fecha del plan &lt;= corte.
     * Se usa la cantidad del insumo y no {@code cantidad_tn}, que esta redondeada a 4
     * decimales en TN: asi queda en la unidad del articulo y sin error de redondeo.
     * Filas ordenadas por nombre de zona.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<Long, ZoneBalanceRow>> queryZoneBalances(List<String> codes, Date cutoff,
                                                                     Long excludeProductionId) {
        Map<String, Map<Long, ZoneBalanceRow>> byItem = new LinkedHashMap<String, Map<Long, ZoneBalanceRow>>();

        List<Object[]> collected = em.createQuery(
                "select cm.metaProduct.productItemCode, pz.id, pz.number, pz.name, sum(cm.balanceWeight) " +
                "from CollectMaterial cm join cm.productiveZone pz " +
                "where cm.state in (:apr, :conta) and cm.date <= :cutoff " +
                "and cm.metaProduct.productItemCode in (:codes) " +
                "group by cm.metaProduct.productItemCode, pz.id, pz.number, pz.name")
                .setParameter("apr", CollectMaterialState.APR)
                .setParameter("conta", CollectMaterialState.CONTA)
                .setParameter("cutoff", cutoff)
                .setParameter("codes", codes)
                .getResultList();
        for (Object[] r : collected) {
            zoneRow(byItem, r).addCollected((BigDecimal) r[4]);
        }

        Query consumedQuery = em.createQuery(
                "select s.productItemCode, pz.id, pz.number, pz.name, sum(s.quantity * z.porcentaje) " +
                "from XSupply s join s.production pr left join pr.productionPlan pl join s.formulationInput fi, " +
                "XProductionBaritinaZona z join z.productiveZone pz " +
                "where z.production = pr and fi.inputDefault = :inputDefault and pr.state <> :anl and " +
                PRODUCTION_DATE_FILTER +
                "and s.productItemCode in (:codes) " +
                (excludeProductionId != null ? "and pr.id <> :exclude " : "") +
                "group by s.productItemCode, pz.id, pz.number, pz.name")
                .setParameter("inputDefault", Boolean.TRUE)
                .setParameter("anl", ProductionState.ANL)
                .setParameter("cutoff", cutoff)
                .setParameter("codes", codes);
        if (excludeProductionId != null) {
            consumedQuery.setParameter("exclude", excludeProductionId);
        }
        for (Object[] r : (List<Object[]>) consumedQuery.getResultList()) {
            BigDecimal qtyByPct = (BigDecimal) r[4];
            zoneRow(byItem, r).addConsumed(qtyByPct != null ? qtyByPct.movePointLeft(2) : null);
        }

        for (Map.Entry<String, Map<Long, ZoneBalanceRow>> entry : byItem.entrySet()) {
            List<ZoneBalanceRow> sorted = new ArrayList<ZoneBalanceRow>(entry.getValue().values());
            Collections.sort(sorted, new Comparator<ZoneBalanceRow>() {
                public int compare(ZoneBalanceRow a, ZoneBalanceRow b) {
                    return a.getZoneName().compareToIgnoreCase(b.getZoneName());
                }
            });
            Map<Long, ZoneBalanceRow> ordered = new LinkedHashMap<Long, ZoneBalanceRow>();
            for (ZoneBalanceRow zone : sorted) {
                ordered.put(zone.getZoneId(), zone);
            }
            entry.setValue(ordered);
        }
        return byItem;
    }

    /** Fila (articulo, zona) de una fila de resultado [codArt, idZona, numero, nombre, ...]; la crea si falta. */
    private ZoneBalanceRow zoneRow(Map<String, Map<Long, ZoneBalanceRow>> byItem, Object[] r) {
        String code = (String) r[0];
        Long zoneId = (Long) r[1];
        Map<Long, ZoneBalanceRow> zones = byItem.get(code);
        if (zones == null) {
            zones = new LinkedHashMap<Long, ZoneBalanceRow>();
            byItem.put(code, zones);
        }
        ZoneBalanceRow zone = zones.get(zoneId);
        if (zone == null) {
            String number = (String) r[2];
            String name = (String) r[3];
            zone = new ZoneBalanceRow(zoneId, number != null ? number + "-" + name : name);
            zones.put(zoneId, zone);
        }
        return zone;
    }

    /** Fin del dia (23:59:59.999) de la fecha dada, usado como corte inclusivo "hasta la fecha". */
    private Date endOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    /** Convierte un valor en TN a la unidad del articulo (KG = x1000; otra = se asume TN). */
    private BigDecimal tnToUnit(BigDecimal tn, String measureCode) {
        if (tn == null) {
            return BigDecimal.ZERO;
        }
        if ("KG".equalsIgnoreCase(measureCode)) {
            return tn.multiply(THOUSAND);
        }
        return tn;
    }

    /** Aplica sumas (codArt, cantidad) sobre las filas existentes; ignora codigos ajenos al almacen. */
    private void applySums(Map<String, WarehouseBalanceRow> rows, List<Object[]> data, boolean add) {
        for (Object[] r : data) {
            WarehouseBalanceRow row = rows.get((String) r[0]);
            if (row == null) {
                continue;
            }
            if (add) {
                row.add((BigDecimal) r[1]);
            } else {
                row.subtract((BigDecimal) r[1]);
            }
        }
    }
}
