package com.encens.khipus.action.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.RHMark;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.production.RawMaterialCollectionSessionService;
import com.encens.khipus.service.production.RawMaterialProducerService;
import com.encens.khipus.util.DateUtils;
import org.apache.poi.hssf.record.formula.functions.T;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Name("rawMaterialCollectionSessionAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialCollectionSessionAction extends GenericAction<RawMaterialCollectionSession> {

    private Calendar startPeriod ;
    private Calendar endPeriod ;
    @In
    private RawMaterialProducerService rawMaterialProducerService;

    @In
    private RawMaterialCollectionSessionService rawMaterialCollectionSessionService;

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    protected GenericService getService() {
        return rawMaterialCollectionSessionService;
    }

    @Factory(value = "rawMaterialCollectionSession", scope = ScopeType.STATELESS)
    public RawMaterialCollectionSession initRawMaterialCollectionSession() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "date";
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startCreate() {
        return Outcome.SUCCESS;
    }

    @Override
    @End
    public String create() {
        try {

            RawMaterialCollectionSession session = getInstance();
            List<Object[]> result = em.createQuery("select s from RawMaterialCollectionSession s where s.date = :date and s.productiveZone = :productiveZone")
                    .setParameter("date", session.getDate())
                    .setParameter("productiveZone", session.getProductiveZone())
                    .getResultList();

            if(result.size()>0)
            {
                addDuplicateDateMessage(session);
                return Outcome.REDISPLAY;
            }
            getService().create(session);
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Override
    public String select(RawMaterialCollectionSession instance) {

        try {
            if(startPeriod == null ||endPeriod == null)
            {
                startPeriod = Calendar.getInstance();
                endPeriod = Calendar.getInstance();
                startPeriod.setTime(DateUtils.getFirsDayFromPeriod(instance.getDate()));
                endPeriod.setTime(DateUtils.getLastDayFromPeriod(instance.getDate()));
            }
            Calendar dateSession = Calendar.getInstance();
            dateSession.setTime(instance.getDate());

            if(DateUtils.isMajor(dateSession,endPeriod))
            {
                addDateSessionMajorDatePeriodMessage(dateSession.getTime());
                return Outcome.FAIL;
            }
            if(DateUtils.isLess(dateSession,startPeriod))
            {
                addDateSessionLessDatePeriodMessaje(dateSession.getTime());
                return Outcome.FAIL;
            }

            rawMaterialCollectionSessionService.updateRawMaterialProducer(getService().findById(getEntityClass(), getId(instance)),instance.getProductiveZone());
            setOp(OP_UPDATE);
            //define the unmanaged instance as current instance
            this.setInstance(instance);
            //Ensure the instance exists in the database, find it
            setInstance(getService().findById(getEntityClass(), getId(instance)));
            startPeriod.setTime(DateUtils.getFirsDayFromPeriod(instance.getDate()));
            endPeriod.setTime(DateUtils.getLastDayFromPeriod(instance.getDate()));
            return Outcome.SUCCESS;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    private void addDateSessionLessDatePeriodMessaje(Date dateSession) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialCollectionSession.DateSessionLessDatePeriodMessaje",dateSession);
    }

    private void addDateSessionMajorDatePeriodMessage(Date dateSession) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialCollectionSession.DateSessionMajorDatePeriodMessage",dateSession);
    }

    @End
    public String jumpNextDate(ProductiveZone productiveZone, Date dateConcurrent,MetaProduct metaProduct,List<CollectedRawMaterial> collectedRawMaterials) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateConcurrent);
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);

            if(endPeriod == null || startPeriod == null){
                startPeriod = Calendar.getInstance();
                endPeriod = Calendar.getInstance();

                startPeriod.setTime(DateUtils.getFirsDayFromPeriod(dateConcurrent));
                endPeriod.setTime(DateUtils.getLastDayFromPeriod(dateConcurrent));
            }


                List<RawMaterialCollectionSession> rawMaterialCollectionSessions = rawMaterialCollectionSessionService.getRawMaterialCollectionSessionByDateAndProductiveZone(productiveZone, calendar.getTime(), endPeriod.getTime());
                RawMaterialCollectionSession nextInstance;
                if (rawMaterialCollectionSessions.size() == 0) {
                    nextInstance = new RawMaterialCollectionSession();
                    nextInstance.setProductiveZone(productiveZone);
                    nextInstance.setDate(calendar.getTime());
                    nextInstance.setMetaProduct(metaProduct);
                    for (CollectedRawMaterial rawMaterial : collectedRawMaterials) {
                        CollectedRawMaterial material = new CollectedRawMaterial();
                        material.setCompany(rawMaterial.getCompany());
                        material.setRawMaterialCollectionSession(nextInstance);
                        material.setRawMaterialProducer(rawMaterial.getRawMaterialProducer());
                        material.setRawMaterialProducerLastName(rawMaterial.getRawMaterialProducerLastName());
                        material.setAmount(0.0);
                        nextInstance.getCollectedRawMaterialList().add(material);
                    }
                    this.setInstance(nextInstance);
                    setOp(OP_CREATE);
                    //Ensure the instance exists in the database, find it
                } else {
                    nextInstance = rawMaterialCollectionSessions.get(0);
                    this.setInstance(nextInstance);
                    //Ensure the instance exists in the database, find it
                    setInstance(getService().findById(getEntityClass(), getId(nextInstance)));
                    setOp(OP_UPDATE);
                }

            return Outcome.REDISPLAY;


        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @End
    public String nextDate(ProductiveZone productiveZone, Date dateConcurrent,MetaProduct metaProduct,List<CollectedRawMaterial> collectedRawMaterials) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateConcurrent);
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);


            if(endPeriod == null || startPeriod == null){
                startPeriod = Calendar.getInstance();
                endPeriod = Calendar.getInstance();

                startPeriod.setTime(DateUtils.getFirsDayFromPeriod(dateConcurrent));
                endPeriod.setTime(DateUtils.getLastDayFromPeriod(dateConcurrent));
            }
            Boolean isEndPeriod = DateUtils.dateEquals(endPeriod,calendar);
            RawMaterialCollectionSession instance = getInstance();

            if(instance.getId() != null)
            {
                this.update();
                if(isEndPeriod)
                {
                    return Outcome.SUCCESS;
                }
            }else{
                String result = this.create();
                if(result != Outcome.SUCCESS)
                {
                    return result;
                }
                if(isEndPeriod)
                {
                    return Outcome.SUCCESS;
                }
            }

            List<RawMaterialCollectionSession>  rawMaterialCollectionSessions = rawMaterialCollectionSessionService.getNextRawMaterialCollectionSessionByDateAndProductiveZone(productiveZone,calendar.getTime());
            RawMaterialCollectionSession nextInstance;
            if(rawMaterialCollectionSessions.size() == 0)
            {
                nextInstance = new RawMaterialCollectionSession();
                nextInstance.setProductiveZone(productiveZone);
                nextInstance.setDate(calendar.getTime());
                nextInstance.setMetaProduct(metaProduct);
                for(CollectedRawMaterial rawMaterial:collectedRawMaterials)
                {
                    CollectedRawMaterial material = new CollectedRawMaterial();
                    material.setCompany(rawMaterial.getCompany());
                    material.setRawMaterialCollectionSession(nextInstance);
                    material.setRawMaterialProducer(rawMaterial.getRawMaterialProducer());
                    material.setRawMaterialProducerLastName(rawMaterial.getRawMaterialProducerLastName());
                    material.setAmount(0.0);
                    nextInstance.getCollectedRawMaterialList().add(material);
                }
                this.setInstance(nextInstance);
                setOp(OP_CREATE);
                //Ensure the instance exists in the database, find it
            }else{
                nextInstance = rawMaterialCollectionSessions.get(0);
                this.setInstance(nextInstance);
                //Ensure the instance exists in the database, find it
                setInstance(getService().findById(getEntityClass(), getId(nextInstance)));
                setOp(OP_UPDATE);
            }


            return Outcome.REDISPLAY;


        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @End
    public String postDate(ProductiveZone productiveZone, Date dateConcurrent) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateConcurrent);
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)-1);


            if(endPeriod == null || startPeriod == null){
                startPeriod = Calendar.getInstance();
                endPeriod = Calendar.getInstance();

                startPeriod.setTime(DateUtils.getFirsDayFromPeriod(dateConcurrent));
                endPeriod.setTime(DateUtils.getLastDayFromPeriod(dateConcurrent));
            }
            Boolean isStartPeriod = DateUtils.dateEquals(startPeriod,calendar);

            List<RawMaterialCollectionSession>  rawMaterialCollectionSessions = rawMaterialCollectionSessionService.getRawMaterialCollectionSessionByDateAndProductiveZone(productiveZone,startPeriod.getTime(),calendar.getTime());
            RawMaterialCollectionSession postSession = rawMaterialCollectionSessions.get(0);
            RawMaterialCollectionSession instance = getInstance();

            if(instance.getId() != null)
            {
                this.update();
                setOp(OP_UPDATE);
                if(isStartPeriod)
                {
                    return Outcome.SUCCESS;
                }
                //define the unmanaged instance as current instance
                this.setInstance(postSession);
                //Ensure the instance exists in the database, find it
                setInstance(getService().findById(getEntityClass(), getId(postSession)));
            }else{
                this.create();
                setOp(OP_CREATE);
                String result = this.create();
                if(result != Outcome.SUCCESS)
                {
                    return result;
                }
                if(isStartPeriod)
                {
                    return Outcome.SUCCESS;
                }
                //define the unmanaged instance as current instance
                this.setInstance(postSession);
            }

            return Outcome.REDISPLAY;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @End
    public String jumpPostDate(ProductiveZone productiveZone, Date dateConcurrent) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateConcurrent);
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)-1);

            if(endPeriod == null || startPeriod == null){
                startPeriod = Calendar.getInstance();
                endPeriod = Calendar.getInstance();

                startPeriod.setTime(DateUtils.getFirsDayFromPeriod(dateConcurrent));
                endPeriod.setTime(DateUtils.getLastDayFromPeriod(dateConcurrent));
            }

            List<RawMaterialCollectionSession>  rawMaterialCollectionSessions = rawMaterialCollectionSessionService.getRawMaterialCollectionSessionByDateAndProductiveZone(productiveZone,calendar.getTime());
            RawMaterialCollectionSession postSession = rawMaterialCollectionSessions.get(0);
            RawMaterialCollectionSession instance = getInstance();

            if(instance.getId() != null)
            {
                //define the unmanaged instance as current instance
                this.setInstance(postSession);
                //Ensure the instance exists in the database, find it
                setInstance(getService().findById(getEntityClass(), getId(postSession)));
                setOp(OP_UPDATE);
            }else{
                //define the unmanaged instance as current instance
                this.setInstance(postSession);
                setOp(OP_CREATE);
            }

            return Outcome.REDISPLAY;

        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    protected void addDuplicateDateMessage(RawMaterialCollectionSession session) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "RawMaterialCollectionSession.message.duplicateDate", session.getDate());
    }

    public double getTotalAmount() {
        double total = 0.0;
        for(CollectedRawMaterial cm : getInstance().getCollectedRawMaterialList()) {
            total += cm.getAmount();
        }
        return total;
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        RawMaterialCollectionSession session = getInstance();
        session.setProductiveZone(productiveZone);
        session.getCollectedRawMaterialList().clear();
        for (RawMaterialProducer producer : rawMaterialProducerService.findAll(productiveZone)) {
            CollectedRawMaterial rawMaterial = new CollectedRawMaterial();
            rawMaterial.setAmount(0.0);
            rawMaterial.setRawMaterialProducer(producer);
            rawMaterial.setRawMaterialCollectionSession(session);
            session.getCollectedRawMaterialList().add(rawMaterial);
        }
    }

    public boolean isPending() {
        return ProductionCollectionState.PENDING.equals(getInstance().getState());
    }

    public boolean isStartPeriod(Date date)
    {
         if(date == null) {
             return false;
         }else
         if(startPeriod == null)
         {
             startPeriod = Calendar.getInstance();
             endPeriod = Calendar.getInstance();

             startPeriod.setTime(DateUtils.getFirsDayFromPeriod(date));
             endPeriod.setTime(DateUtils.getLastDayFromPeriod(date));
         }

             Calendar calendar = Calendar.getInstance();
             calendar.setTime(date);
             if (DateUtils.dateEquals(startPeriod,calendar)) {
                 return true;
             }

            return false;
    }

    public boolean isEndPeriod(Date date)
    {
        if(date == null) {
            return false;
        }else
        if(startPeriod == null)
        {
            startPeriod = Calendar.getInstance();
            endPeriod = Calendar.getInstance();

            startPeriod.setTime(DateUtils.getFirsDayFromPeriod(date));
            endPeriod.setTime(DateUtils.getLastDayFromPeriod(date));
        }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (DateUtils.dateEquals(endPeriod,calendar)) {
                return true;
            }
        return false;
    }
}
