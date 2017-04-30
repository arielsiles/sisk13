package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Name("rawMaterialCollectionSessionService")
@Stateless
@AutoCreate
public class RawMaterialCollectionSessionServiceBean extends ExtendedGenericServiceBean implements RawMaterialCollectionSessionService {

    @In
    private ProductiveZoneService productiveZoneService;

    @Override
    protected Object preCreate(Object entity) {
        updateCollectionRecord((RawMaterialCollectionSession) entity);
        return entity;
    }

    private void updateCollectionRecord(RawMaterialCollectionSession entity) {
        RawMaterialCollectionSession session = (RawMaterialCollectionSession)entity;
        double totalAmount = sumCollectedAmount(session);
        updateReceivedAmountOnCollectionRecord(session, totalAmount);
    }

    private void updateReceivedAmountOnCollectionRecord(RawMaterialCollectionSession session, double totalAmount) {
        CollectionRecord record = findCollectionRecord(session);
        if (record == null) {
            return;
        }

        record.setReceivedAmount(totalAmount);
    }

    @Override
    protected Object preUpdate(Object entity) {
        updateCollectionRecord((RawMaterialCollectionSession) entity);
        return entity;
    }

    @Override
    protected Object preDelete(Object entity) {
        RawMaterialCollectionSession session = (RawMaterialCollectionSession)entity;
        updateReceivedAmountOnCollectionRecord(session, 0.0);
        return entity;
    }

    private CollectionRecord findCollectionRecord(RawMaterialCollectionSession rawMaterialCollectionSession) {
        try {
            CollectionRecord record = (CollectionRecord) getEntityManager().createNamedQuery("CollectionRecord.findByDateAndProductiveZoneAndMetaProduct")
                                                                            .setParameter("date", rawMaterialCollectionSession.getDate())
                                                                            .setParameter("productiveZone", rawMaterialCollectionSession.getProductiveZone())
                                                                            .setParameter("metaProduct", rawMaterialCollectionSession.getMetaProduct())
                                                                            .getSingleResult();
            return record;
        } catch (NoResultException ex) {
            return null;
        }
    }

    private double sumCollectedAmount(RawMaterialCollectionSession rawMaterialCollectionSession) {
        double total = 0.0;
        for (CollectedRawMaterial rawMaterial : rawMaterialCollectionSession.getCollectedRawMaterialList()) {
            total += rawMaterial.getAmount();
        }
        return total;
    }

    public void updateRawMaterialProducer(RawMaterialCollectionSession rawMaterialCollectionSession,ProductiveZone productiveZone)
    {

        List<RawMaterialProducer> datas = getEntityManager().createQuery("SELECT rawMaterialProducer " +
                "from RawMaterialProducer rawMaterialProducer where rawMaterialProducer.productiveZone = :productiveZone")
                .setParameter("productiveZone", productiveZone)
                .getResultList();

        if(datas.size() > rawMaterialCollectionSession.getCollectedRawMaterialList().size())
        {
            for(CollectedRawMaterial collectedRawMaterial : rawMaterialCollectionSession.getCollectedRawMaterialList())
            {
                datas.remove(collectedRawMaterial.getRawMaterialProducer());
            }
            for(RawMaterialProducer rawMaterialProducer: datas)
            {
                CollectedRawMaterial aux = rawMaterialCollectionSession.getCollectedRawMaterialList().get(0);
                CollectedRawMaterial collectedRawMaterial = new CollectedRawMaterial();
                collectedRawMaterial.setAmount(0.0);
                collectedRawMaterial.setCompany(aux.getCompany());
                collectedRawMaterial.setRawMaterialCollectionSession(aux.getRawMaterialCollectionSession());
                collectedRawMaterial.setRawMaterialProducer(rawMaterialProducer);
                collectedRawMaterial.setVersion(aux.getVersion());
                getEntityManager().persist(collectedRawMaterial);
            }

            getEntityManager().flush();
            getEntityManager().refresh(rawMaterialCollectionSession);
        }
    }

    @Override
    public List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date startDate, Date endDate) {
        List<RawMaterialCollectionSession> rawMaterialCollectionSessions = new ArrayList<RawMaterialCollectionSession>();

        try{
            rawMaterialCollectionSessions = (List<RawMaterialCollectionSession>)getEntityManager().createQuery("select rawMaterialCollectionSession from RawMaterialCollectionSession rawMaterialCollectionSession" +
                    " where rawMaterialCollectionSession.date between :startDate and :endDate" +
                    " and rawMaterialCollectionSession.productiveZone = :productiveZone order by rawMaterialCollectionSession.date desc")
                                           .setParameter("startDate", startDate, TemporalType.DATE)
                                           .setParameter("endDate", endDate, TemporalType.DATE)
                                           .setParameter("productiveZone", productiveZone)
                                           .getResultList();
        }catch(NoResultException e){

        }

        return rawMaterialCollectionSessions;
    }

    @Override
    public List<RawMaterialCollectionSession> getNextRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date date) {
        List<RawMaterialCollectionSession> rawMaterialCollectionSessions = new ArrayList<RawMaterialCollectionSession>();

        try{
            rawMaterialCollectionSessions = (List<RawMaterialCollectionSession>)getEntityManager().createQuery("select rawMaterialCollectionSession from RawMaterialCollectionSession rawMaterialCollectionSession" +
                    " where rawMaterialCollectionSession.date = :date" +
                    " and rawMaterialCollectionSession.productiveZone = :productiveZone order by rawMaterialCollectionSession.date desc")
                    .setParameter("date", date, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .getResultList();
        }catch(NoResultException e){

        }

        return rawMaterialCollectionSessions;
    }

    @Override
    public List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date date) {
        List<RawMaterialCollectionSession> rawMaterialCollectionSessions = new ArrayList<RawMaterialCollectionSession>();

        try{
            rawMaterialCollectionSessions = (List<RawMaterialCollectionSession>)getEntityManager().createQuery("select rawMaterialCollectionSession from RawMaterialCollectionSession rawMaterialCollectionSession" +
                    " where rawMaterialCollectionSession.date < :date " +
                    " and rawMaterialCollectionSession.productiveZone = :productiveZone order by rawMaterialCollectionSession.date desc")
                    .setParameter("date", date, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .getResultList();
        }catch(NoResultException e){

        }

        return rawMaterialCollectionSessions;
    }
}
