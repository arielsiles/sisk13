package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionUlexita;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
@Name("xproductionUlexitaService")
@AutoCreate
public class XProductionUlexitaServiceBean implements XProductionUlexitaService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public XProductionUlexita findByProduction(XProduction production) {
        if (production == null || production.getId() == null) return null;
        try {
            return (XProductionUlexita) em.createQuery(
                    "select u from XProductionUlexita u where u.production.id = :pid")
                    .setParameter("pid", production.getId())
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public void save(XProductionUlexita ulexita) {
        if (ulexita == null) return;
        if (ulexita.getId() == null) {
            em.persist(ulexita);
        } else {
            em.merge(ulexita);
        }
        em.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XProduction> findProductionsByLineAndMonth(ProductionLine line, int year, int month) {
        if (line == null) return Collections.emptyList();
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month - 1, 1, 0, 0, 0);
        Date from = c.getTime();
        c.add(Calendar.MONTH, 1);
        Date to = c.getTime();

        Query q = em.createQuery(
                "select p from XProduction p " +
                "where p.productionLine = :line " +
                "  and p.initDate >= :from " +
                "  and p.initDate <  :to " +
                "order by p.initDate asc, p.id asc");
        q.setParameter("line", line);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    public void persistSnapshots(XProduction production, BigDecimal ulexAvailable, String userCode) {
        if (production == null) return;
        XProductionUlexita u = findByProduction(production);
        if (u == null) return;

        if (ulexAvailable != null) {
            u.setUlexDisponibleSnap(ulexAvailable);
        }

        // forceLive=true: durante la generacion del snapshot leemos siempre
        // los valores frescos calculados desde inputs y configuracion actual,
        // ignorando snapshots previos (caso de re-aprobacion).
        XProductionUlexitaCalc calc = new XProductionUlexitaCalc(
                production, u, production.getProductionLine(),
                production.getSupplyList(), production.getProductionProductList(),
                true);

        u.setMermaFactorSnap(calc.getMermaFactor());
        u.setDiluyenteTotalSnap(calc.getDiluyenteTotal());
        u.setBentonitaPctSnap(calc.getBentonitaPct());
        u.setCaolinPctSnap(calc.getCaolinPct());
        u.setPtASnap(calc.getPtA());
        u.setPtBSnap(calc.getPtB());
        u.setPtTotalBuenoSnap(calc.getPtTotalBueno());
        u.setKpaSnap(calc.getKpa());
        u.setKpmBentonitaSnap(calc.getKpmBentonita());
        u.setKpmMermaSnap(calc.getKpmMerma());
        u.setLeyMpRecalcSnap(calc.getLeyMpRecalc());
        u.setMermaSnap(calc.getMerma());
        u.setMermaPctSnap(calc.getMermaPct());
        u.setConsumoMpCalcSnap(calc.getConsumoMpCalc());
        u.setSnapAt(new java.util.Date());
        u.setSnapBy(userCode);

        em.merge(u);
        em.flush();
    }
}
