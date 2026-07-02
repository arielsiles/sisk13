package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.warehouse.MovementDetailType;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<WarehouseBalanceRow> computeBalances(String companyNumber, String warehouseCode) {
        Map<String, WarehouseBalanceRow> rows = new LinkedHashMap<String, WarehouseBalanceRow>();
        if (companyNumber == null || warehouseCode == null) {
            return new ArrayList<WarehouseBalanceRow>();
        }

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
                "group by md.productItemCode, md.movementType")
                .setParameter("cn", companyNumber)
                .setParameter("wc", warehouseCode)
                .setParameter("apr", WarehouseVoucherState.APR)
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
                "where cm.state in (:apr, :conta) " +
                "group by cm.metaProduct.productItemCode")
                .setParameter("apr", CollectMaterialState.APR)
                .setParameter("conta", CollectMaterialState.CONTA)
                .getResultList();
        applySums(rows, collect, true);

        // 4) Produccion: Producto Terminado producido (entrada). Orden != ANL.
        List<Object[]> produced = em.createQuery(
                "select pp.productItemCode, sum(pp.quantity) from XProductionProduct pp " +
                "where pp.production.state <> :anl " +
                "group by pp.productItemCode")
                .setParameter("anl", ProductionState.ANL)
                .getResultList();
        applySums(rows, produced, true);

        // 5) Produccion: consumo de insumos (salida). Incluye PT usado como insumo. Orden != ANL.
        List<Object[]> consumed = em.createQuery(
                "select s.productItemCode, sum(s.quantity) from XSupply s " +
                "where s.production.state <> :anl " +
                "group by s.productItemCode")
                .setParameter("anl", ProductionState.ANL)
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
                "where u.codArtReprocFinal is not null and u.production.state <> :anl " +
                "group by u.codArtReprocFinal")
                .setParameter("anl", ProductionState.ANL)
                .getResultList();
        for (Object[] r : reproceso) {
            WarehouseBalanceRow row = rows.get((String) r[0]);
            if (row == null) {
                continue;
            }
            row.add(tnToUnit((BigDecimal) r[1], row.getMeasureCode()));      // Reproceso final -> entrada
            row.subtract(tnToUnit((BigDecimal) r[2], row.getMeasureCode())); // Consumo reproceso -> salida
        }

        return new ArrayList<WarehouseBalanceRow>(rows.values());
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
