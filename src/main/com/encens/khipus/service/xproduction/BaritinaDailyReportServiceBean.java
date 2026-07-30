package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
@Name("baritinaDailyReportService")
@AutoCreate
public class BaritinaDailyReportServiceBean implements BaritinaDailyReportService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<XProduction> findProductions(ProductionLine line, Date from, Date to) {
        if (line == null || from == null || to == null) return Collections.emptyList();
        // Las ordenes anuladas no se cuentan: no producen ni consumen nada. Mismo criterio que
        // XProductionBalanceService, para que el reporte y "Saldos de Almacen" no discrepen.
        Query q = em.createQuery(
                "select p from XProduction p " +
                "where p.productionLine = :line " +
                "  and p.state <> :anl " +
                "  and p.initDate >= :from and p.initDate < :to " +
                "order by p.initDate asc, p.id asc");
        q.setParameter("line", line);
        q.setParameter("anl", ProductionState.ANL);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> sumAcopioByDay(String codArt, Date from, Date to) {
        if (codArt == null || from == null || to == null) return Collections.emptyList();
        Query q = em.createQuery(
                "select c.date, sum(c.balanceWeight) from CollectMaterial c " +
                "where c.metaProduct.productItemCode = :cod " +
                "  and c.date >= :from and c.date < :to " +
                "  and c.state in (:apr, :conta) " +
                "group by c.date order by c.date");
        q.setParameter("cod", codArt);
        q.setParameter("from", from);
        q.setParameter("to", to);
        q.setParameter("apr", CollectMaterialState.APR);
        q.setParameter("conta", CollectMaterialState.CONTA);
        return q.getResultList();
    }

    @Override
    public BigDecimal sumAcopioBefore(String codArt, Date before) {
        if (codArt == null || before == null) return BigDecimal.ZERO;
        Object r = em.createQuery(
                "select sum(c.balanceWeight) from CollectMaterial c " +
                "where c.metaProduct.productItemCode = :cod " +
                "  and c.date < :before and c.state in (:apr, :conta)")
                .setParameter("cod", codArt)
                .setParameter("before", before)
                .setParameter("apr", CollectMaterialState.APR)
                .setParameter("conta", CollectMaterialState.CONTA)
                .getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> dispatchRows(Collection<String> codArts, Date from, Date to) {
        if (codArts == null || codArts.isEmpty() || from == null || to == null) return Collections.emptyList();
        Query q = em.createQuery(
                "select det.dispatch.dispatchDate, det.quantity " +
                "from WarehouseVoucherDispatchDetail det " +
                "where det.productItemCode in (:cods) " +
                "  and det.dispatch.dispatchDate >= :from and det.dispatch.dispatchDate < :to " +
                "  and det.dispatch.state in (:apr, :fin)");
        q.setParameter("cods", codArts);
        q.setParameter("from", from);
        q.setParameter("to", to);
        q.setParameter("apr", DispatchState.APROBADO);
        q.setParameter("fin", DispatchState.FINALIZADO);
        return q.getResultList();
    }

    @Override
    public BigDecimal sumDispatchBefore(Collection<String> codArts, Date before) {
        if (codArts == null || codArts.isEmpty() || before == null) return BigDecimal.ZERO;
        Object r = em.createQuery(
                "select sum(det.quantity) from WarehouseVoucherDispatchDetail det " +
                "where det.productItemCode in (:cods) " +
                "  and det.dispatch.dispatchDate < :before " +
                "  and det.dispatch.state in (:apr, :fin)")
                .setParameter("cods", codArts)
                .setParameter("before", before)
                .setParameter("apr", DispatchState.APROBADO)
                .setParameter("fin", DispatchState.FINALIZADO)
                .getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> adjustmentRows(Collection<String> codArts, Date from, Date to) {
        if (codArts == null || codArts.isEmpty() || from == null || to == null) return Collections.emptyList();
        // Se excluyen los movimientos cuyo vale fue generado por un despacho: el despacho
        // se enlaza con el vale por (no_cia_vale, no_trans_vale) y ya tiene su propia columna.
        Query q = em.createQuery(
                "select md.movementDetailDate, md.movementType, md.quantity, " +
                "       md.productItemCode, md.transactionNumber " +
                "from MovementDetail md " +
                "where md.productItemCode in (:cods) " +
                "  and md.state = :apr " +
                "  and md.movementDetailDate >= :from and md.movementDetailDate < :to " +
                "  and not exists (select d.id from WarehouseVoucherDispatch d "+
                "                  where d.warehouseVoucherTransactionNumber = md.transactionNumber " +
                "                    and d.warehouseVoucherCompanyNumber = md.companyNumber) " +
                "order by md.movementDetailDate");
        q.setParameter("cods", codArts);
        q.setParameter("apr", WarehouseVoucherState.APR);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    public boolean isSharedMaterial(String codArt, ProductionLine line) {
        if (codArt == null || line == null) return false;
        Number n = (Number) em.createQuery(
                "select count(l) from ProductionLine l " +
                "where l.codArtMpPrincipal = :cod and l <> :line")
                .setParameter("cod", codArt)
                .setParameter("line", line)
                .getSingleResult();
        return n != null && n.intValue() > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> supplyRowsOtherLines(String codArt, ProductionLine line, Date from, Date to) {
        if (codArt == null || line == null || from == null || to == null) return Collections.emptyList();
        // Se devuelven las dos fechas sin agregar: el llamador elige plan (o initDate como
        // respaldo) y agrupa por dia. Evita coalesce dentro de group by, que no es terreno
        // seguro en Hibernate 3, y el volumen por articulo y mes es chico.
        Query q = em.createQuery(
                "select pl.date, pr.initDate, s.quantity " +
                "from XSupply s left join s.production pr left join pr.productionPlan pl " +
                "where s.productItemCode = :cod " +
                "  and pr.state <> :anl " +
                "  and (pr.productionLine is null or pr.productionLine <> :line) " +
                "  and ((pl.date is not null and pl.date >= :from and pl.date < :to) " +
                "    or (pl.date is null and pr.initDate >= :from and pr.initDate < :to))");
        q.setParameter("cod", codArt);
        q.setParameter("anl", ProductionState.ANL);
        q.setParameter("line", line);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }
}
