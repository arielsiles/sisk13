package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.SalaryMovementProducerException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.RoundUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("salaryMovementProducerService")
@Stateless
@AutoCreate
public class SalaryMavementProducerServiceBean extends ExtendedGenericServiceBean implements SalaryMovementProducerService {

    @Override
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer, Date startDate, Date endDate,ProductiveZone productiveZone) throws EntryNotFoundException {

        RawMaterialProducerDiscount rawMaterialProducerDiscount = new RawMaterialProducerDiscount();

        rawMaterialProducerDiscount.setRawMaterialProducer(rawMaterialProducer);
        rawMaterialProducerDiscount.setConcentrated(0.0);
        rawMaterialProducerDiscount.setYogurt(0.0);
        rawMaterialProducerDiscount.setVeterinary(0.0);
        rawMaterialProducerDiscount.setCredit(0.0);
        rawMaterialProducerDiscount.setCans(0.0);
        rawMaterialProducerDiscount.setOtherDiscount(0.0);
        rawMaterialProducerDiscount.setOtherIncoming(0.0);

        Double concentrated = 0.0;
        Double yogurt = 0.0;
        Double veterinary = 0.0;
        Double credit = 0.0;
        Double cans = 0.0;
        Double otherDiscount = 0.0;
        Double otherIncoming = 0.0;
        //Todo: se toma en cuenta tambien el gab en caso que el productor haya sido movido de gab
        List<Object[]> salaryMovementProducers = getEntityManager().createNamedQuery("SalaryMovementProducer.getDiscount")
                                                               .setParameter("startDate", startDate, TemporalType.DATE)
                                                               .setParameter("endDate",endDate,TemporalType.DATE)
                                                               .setParameter("rawMaterialProducer",rawMaterialProducer)
                                                               .setParameter("productiveZone",productiveZone)
                                                               .getResultList();
        if(salaryMovementProducers.size()>0)
        {
            for(Object[] salaryMovementProducer: salaryMovementProducers)
            {
                   Double valor = (Double)salaryMovementProducer[0];
                   String typeDiscount = (String)salaryMovementProducer[2];
                       if(typeDiscount.compareTo("CONCENTRADOS")==0)
                       {
                           concentrated += valor;
                       }
                       if(typeDiscount.compareTo("YOGURT")==0)
                       {
                           yogurt += valor;
                       }
                       if(typeDiscount.compareTo("VETERINARIO")==0)
                       {
                           veterinary += valor;
                       }
                       if(typeDiscount.compareTo("TACHOS")==0)
                       {
                           cans += valor;
                       }
                       if(typeDiscount.compareTo("OTROS EGRESOS")==0)
                       {
                           otherDiscount += valor;
                       }
                       if(typeDiscount.compareTo("OTROS INGRESOS")==0)
                       {
                           otherIncoming += valor;
                       }
                       if(typeDiscount.compareTo("CREDITO")==0)
                       {
                           credit += valor;
                       }
            }
        }

        rawMaterialProducerDiscount.setConcentrated(RoundUtil.getRoundValue(concentrated,2, RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setYogurt(RoundUtil.getRoundValue(yogurt,2, RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setVeterinary(RoundUtil.getRoundValue(veterinary,2,RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setCredit(RoundUtil.getRoundValue(credit,2,RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setCans(RoundUtil.getRoundValue(cans,2,RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setOtherDiscount(RoundUtil.getRoundValue(otherDiscount,2,RoundUtil.RoundMode.SYMMETRIC));
        rawMaterialProducerDiscount.setOtherIncoming(RoundUtil.getRoundValue(otherIncoming,2,RoundUtil.RoundMode.SYMMETRIC));

        return rawMaterialProducerDiscount;
    }

    @Override
    public Double getTotalCollectedByProductor(RawMaterialProducer rawMaterialProducer, Date date) {
        Date initDate = DateUtils.getFirsDayFromPeriod(date);
        Date endDate = DateUtils.getLastDayFromPeriod(date);
        Double totalCollected = (Double)getEntityManager().createQuery("SELECT sum(collectedRawMaterial.amount)  FROM RawMaterialCollectionSession rawMaterialCollectionSession" +
                                       " INNER JOIN rawMaterialCollectionSession.collectedRawMaterialList collectedRawMaterial" +
                                       " WHERE rawMaterialCollectionSession.date BETWEEN :initDate AND :endDate " +
                                       " AND collectedRawMaterial.rawMaterialProducer = :rawMaterialProducer")
                                      .setParameter("initDate",initDate,TemporalType.DATE)
                                      .setParameter("endDate",endDate,TemporalType.DATE)
                                      .setParameter("rawMaterialProducer",rawMaterialProducer)
                                      .getSingleResult();
        if(totalCollected == null)
            return 0.0;
        return totalCollected * Constants.PRICE_UNIT_MILK;
    }

    public ProductiveZone getZoneProductiveByProductor(RawMaterialProducer rawMaterialProducer)
    {
        ProductiveZone productiveZone = (ProductiveZone)getEntityManager().createQuery("select productiveZone from RawMaterialProducer rawMaterialProducer where rawMaterialProducer = :rawMaterialProducer")
                                                          .setParameter("rawMaterialProducer",rawMaterialProducer)
                                                          .getSingleResult();
        return  productiveZone;
    }

    private List<RawMaterialPayRoll> getRawMaterialPayRollByGAB(Date initDate,Date endDate,ProductiveZone productiveZone,ProductiveZone productiveZoneMove)
    {
        List<RawMaterialPayRoll> rawMaterialPayRolls = (List<RawMaterialPayRoll>) getEntityManager().createQuery("select rawMaterialPayRoll from RawMaterialPayRoll rawMaterialPayRoll" +
                " where rawMaterialPayRoll.startDate = :startDate " +
                " and rawMaterialPayRoll.endDate = :endDate " +
                " and rawMaterialPayRoll.productiveZone = :productiveZoneConcurrent")
                .setParameter("startDate",initDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .setParameter("productiveZoneConcurrent",productiveZone)
                .getResultList();

        rawMaterialPayRolls.addAll((List<RawMaterialPayRoll>) getEntityManager().createQuery("select rawMaterialPayRoll from RawMaterialPayRoll rawMaterialPayRoll" +
                " where rawMaterialPayRoll.startDate = :startDate " +
                " and rawMaterialPayRoll.endDate = :endDate " +
                " and rawMaterialPayRoll.productiveZone = :productiveZoneMove")
                .setParameter("startDate",initDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .setParameter("productiveZoneMove",productiveZoneMove)
                .getResultList());

        return  rawMaterialPayRolls;
    }

    @Override
    public void moveSessionsProductor(RawMaterialProducer rawMaterialProducer, Date date,ProductiveZone productiveZone) throws SalaryMovementProducerException{
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH,Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH,1);*/
        Date initDate = DateUtils.getFirsDayFromPeriod(date);
       /* Date initDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH,15);*/
        Date endDate = DateUtils.getLastDayFromPeriod(date);
        //Date endDate = calendar.getTime();

        List<RawMaterialPayRoll> rawMaterialPayRolls = getRawMaterialPayRollByGAB(initDate,endDate,productiveZone,rawMaterialProducer.getProductiveZone());

        if(rawMaterialPayRolls.size() > 0)
        {
           throw new SalaryMovementProducerException();
        }else{
            List<RawMaterialCollectionSession> sessionsConcurrents = getRawMaterialCollectionSessionByPeriod(initDate,endDate,productiveZone,rawMaterialProducer);
            List<RawMaterialCollectionSession> moveSessions = getRawMaterialCollectionSessionByPeriodAndGAB(initDate, endDate, rawMaterialProducer.getProductiveZone());

                for(RawMaterialCollectionSession cocurrent:sessionsConcurrents)
                {
                    CollectedRawMaterial collectedRawMaterial = getCollectedRawMaterialByProductor(cocurrent,rawMaterialProducer);
                    boolean aux = true;
                    for(RawMaterialCollectionSession move:moveSessions)
                    {
                        if(move.getDate().compareTo(cocurrent.getDate()) == 0)
                        {


                            getEntityManager().createNativeQuery("update acopiomateriaprima set idsesionacopio = :session where idacopiomateriaprima = :collectedRawMaterial")
                                                                .setParameter("session",move)
                                                                .setParameter("collectedRawMaterial",collectedRawMaterial)
                                                                .executeUpdate();

                            aux = false;
                            continue;
                        }
                    }
                    if(aux)
                    {
                        createNewRawMaterialCollectionSession(rawMaterialProducer.getProductiveZone(),cocurrent,collectedRawMaterial);
                    }
                }

        }

    }

    private CollectedRawMaterial getCollectedRawMaterialByProductor(RawMaterialCollectionSession session,RawMaterialProducer rawMaterialProducer)
    {

            for(CollectedRawMaterial collectedRawMaterial: session.getCollectedRawMaterialList())
            {
                if(rawMaterialProducer == collectedRawMaterial.getRawMaterialProducer())
                {
                    return  collectedRawMaterial;
                }
            }
        return null;
    }

    @Override
    public void moveDiscountsProductor(RawMaterialProducer rawMaterialProducer, Date date, ProductiveZone productiveZone) throws SalaryMovementProducerException{
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH,Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH,1);*/
        Date initDate = DateUtils.getFirsDayFromPeriod(date);
        //Date initDate = calendar.getTime();
        //calendar.set(Calendar.DAY_OF_MONTH,15);
        Date endDate = DateUtils.getLastDayFromPeriod(date);
        //Date endDate = calendar.getTime();

        List<RawMaterialPayRoll> rawMaterialPayRolls = getRawMaterialPayRollByGAB(initDate,endDate,productiveZone,rawMaterialProducer.getProductiveZone());

        if(rawMaterialPayRolls.size() > 0)
        //if(false)
        {
            throw new SalaryMovementProducerException();
        }else{

            getEntityManager().createQuery(" update SalaryMovementProducer set productiveZone = :productiveZone" +
                                           " where date between :startDate and :endDate" +
                                           " and rawMaterialProducer = :rawMaterialProducer")
                                           .setParameter("productiveZone",rawMaterialProducer.getProductiveZone())
                                           .setParameter("startDate",initDate)
                                           .setParameter("endDate",endDate)
                                           .setParameter("rawMaterialProducer",rawMaterialProducer)
                                           .executeUpdate();
        }
    }

    private void createNewRawMaterialCollectionSession(ProductiveZone productiveZone, RawMaterialCollectionSession cocurrent,CollectedRawMaterial collectedRawMaterial) {

        RawMaterialCollectionSession session = new RawMaterialCollectionSession();
        session.setCompany(cocurrent.getCompany());
        session.setProductiveZone(productiveZone);
        session.setMetaProduct(cocurrent.getMetaProduct());
        session.setDate(cocurrent.getDate());
        session.getCollectedRawMaterialList().add(collectedRawMaterial);

        getEntityManager().persist(session);
        getEntityManager().flush();
       /* getEntityManager().createNativeQuery("update acopiomateriaprima set idsesionacopio = :session where idacopiomateriaprima = :collectedRawMaterial")
                .setParameter("session",session)
                .setParameter("collectedRawMaterial",collectedRawMaterial)
                .executeUpdate();*/


    }

    private List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByPeriodAndGAB(Date startDate, Date endDate, ProductiveZone productiveZone) {
        List<RawMaterialCollectionSession> rawMaterialCollectionSessions = new ArrayList<RawMaterialCollectionSession>();

        try{
            rawMaterialCollectionSessions = (List<RawMaterialCollectionSession>)getEntityManager().createQuery("select rawMaterialCollectionSession from RawMaterialCollectionSession rawMaterialCollectionSession" +
                    " inner join rawMaterialCollectionSession.collectedRawMaterialList collectedRawMaterial" +
                    " where rawMaterialCollectionSession.date between :startDate and :endDate" +
                    " and rawMaterialCollectionSession.productiveZone = :productiveZone")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("productiveZone",productiveZone)
                    .getResultList();
        }catch (NoResultException e){

        }
        return rawMaterialCollectionSessions;
    }

    public List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByPeriod(Date startDate,Date endDate, ProductiveZone productiveZone, RawMaterialProducer rawMaterialProducer)
    {
        List<RawMaterialCollectionSession> rawMaterialCollectionSessions = new ArrayList<RawMaterialCollectionSession>();

        try{
            rawMaterialCollectionSessions = (List<RawMaterialCollectionSession>)getEntityManager().createQuery("select rawMaterialCollectionSession from RawMaterialCollectionSession rawMaterialCollectionSession" +
                                           " inner join rawMaterialCollectionSession.collectedRawMaterialList collectedRawMaterial" +
                                           " where rawMaterialCollectionSession.date between :startDate and :endDate" +
                                           " and rawMaterialCollectionSession.productiveZone = :productiveZone" +
                                           " and collectedRawMaterial.rawMaterialProducer = :rawMaterialProducer")
                                           .setParameter("startDate",startDate,TemporalType.DATE)
                                           .setParameter("endDate",endDate,TemporalType.DATE)
                                           .setParameter("productiveZone",productiveZone)
                                           .setParameter("rawMaterialProducer",rawMaterialProducer)
                                           .getResultList();
        }catch (NoResultException e){

        }
        return rawMaterialCollectionSessions;
    }


}
