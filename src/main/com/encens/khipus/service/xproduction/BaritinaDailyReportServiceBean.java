package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.warehouse.DispatchState;
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
        Query q = em.createQuery(
                "select p from XProduction p " +
                "where p.productionLine = :line " +
                "  and p.initDate >= :from and p.initDate < :to " +
                "order by p.initDate asc, p.id asc");
        q.setParameter("line", line);
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
}
