package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.SalaryMovementProducerException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import com.encens.khipus.model.production.*;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.RoundUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private RawMaterialProducerService rawMaterialProducerService;
    @In
    private TypeMovementProducerService typeMovementProducerService;

    @Override
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer, Date startDate, Date endDate,ProductiveZone productiveZone) throws EntryNotFoundException {

        RawMaterialProducerDiscount rawMaterialProducerDiscount = new RawMaterialProducerDiscount();

        rawMaterialProducerDiscount.setRawMaterialProducer(rawMaterialProducer);
        rawMaterialProducerDiscount.setConcentrated(0.0);
        rawMaterialProducerDiscount.setCommission(0.0);
        rawMaterialProducerDiscount.setYogurt(0.0);
        rawMaterialProducerDiscount.setVeterinary(0.0);
        rawMaterialProducerDiscount.setCredit(0.0);
        rawMaterialProducerDiscount.setCans(0.0);
        rawMaterialProducerDiscount.setOtherDiscount(0.0);
        rawMaterialProducerDiscount.setOtherIncoming(0.0);

        Double concentrated = 0.0;
        Double commission = 0.0;
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
                       if(typeDiscount.compareTo("COMISION BANCO")==0)
                       {
                            commission += valor;
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
        rawMaterialProducerDiscount.setCommission(RoundUtil.getRoundValue(commission,2, RoundUtil.RoundMode.SYMMETRIC));
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

    /**
     * Para descuentos Lacteos y Veterinarios
     * @param customerOrder
     */
    @Override
    public void createSalaryMovementProducer(CustomerOrder customerOrder) {

        RawMaterialProducer producer = rawMaterialProducerService.findProducerByIdNumber(customerOrder.getClient().getIdNumber());

        System.out.println("-----------> Productor: " + producer );

        TypeMovementProducer typeMovementProducer = null;
        if (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.VETERINARY))
            typeMovementProducer = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.VETE);
        if (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.MILK))
            typeMovementProducer = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.LACT);

        SalaryMovementProducer salaryMovementProducer = new SalaryMovementProducer();
        salaryMovementProducer.setDate(customerOrder.getOrderDate());
        salaryMovementProducer.setState(SalaryMovementProducerState.PENDIENTE);
        salaryMovementProducer.setValor(customerOrder.getTotalAmount());
        salaryMovementProducer.setSaldo(customerOrder.getTotalAmount());
        salaryMovementProducer.setCompany(producer.getCompany());
        salaryMovementProducer.setProductiveZone(producer.getProductiveZone());
        salaryMovementProducer.setRawMaterialProducer(producer);
        salaryMovementProducer.setTypeMovementProducer(typeMovementProducer);
        salaryMovementProducer.setDescription(MessageUtils.getMessage("SalaryMovementProducer.creditSale.gloss") + " " +
                                              customerOrder.getCode() + " " +
                                              MessageUtils.getMessage(typeMovementProducer.getSalaryMovementProducerTypeEnum().getResourceKey()));

        System.out.println("-----------------> SalaryMovementProducer: " + salaryMovementProducer);

        em.persist(salaryMovementProducer);
        em.flush();
    }

    @Override
    public List<RawMaterialProducer> findProducersWithCollection(Date startDate, Date endDate) {

        List<RawMaterialProducer> rawMaterialProducerList =
        em.createQuery("select distinct c.rawMaterialProducer from CollectedRawMaterial c " +
                          " where c.rawMaterialCollectionSession.date between :startDate and :endDate " +
                          " and c.amount > 0 ")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();

        System.out.println("--------------> PRODUCTORES: " + rawMaterialProducerList.size());

        for (RawMaterialProducer producer : rawMaterialProducerList){
            System.out.println("......:::..." + producer.getFullName());
        }
        return rawMaterialProducerList;
    }

    public void createSalaryMovementProducer(List<SalaryMovementProducer> salaryMovementProducerList){

        for (SalaryMovementProducer salaryMovementProducer : salaryMovementProducerList){
            em.persist(salaryMovementProducer);
            em.flush();
        }

    }

    /**
     * @Claude OPT-4: Pre-carga batch de descuentos para todos los productores de una zona.
     * Reemplaza las llamadas individuales a prepareDiscount() por productor
     * con una sola query SQL que trae todos los movimientos de la zona.
     */
    @Override
    public Map<Long, RawMaterialProducerDiscount> prepareDiscountsBatch(Date startDate, Date endDate, ProductiveZone productiveZone) {
        Map<Long, RawMaterialProducerDiscount> result = new java.util.HashMap<Long, RawMaterialProducerDiscount>();

        List<Object[]> salaryMovements = getEntityManager().createNamedQuery("SalaryMovementProducer.getDiscountByZone")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("productiveZone", productiveZone)
                .getResultList();

        for (Object[] row : salaryMovements) {
            Long producerId = (Long) row[0];
            Double valor = (Double) row[1];
            String typeDiscount = (String) row[3];

            RawMaterialProducerDiscount discount = result.get(producerId);
            if (discount == null) {
                discount = new RawMaterialProducerDiscount();
                discount.setConcentrated(0.0);
                discount.setCommission(0.0);
                discount.setYogurt(0.0);
                discount.setVeterinary(0.0);
                discount.setCredit(0.0);
                discount.setCans(0.0);
                discount.setOtherDiscount(0.0);
                discount.setOtherIncoming(0.0);
                result.put(producerId, discount);
            }

            if ("CONCENTRADOS".equals(typeDiscount)) {
                discount.setConcentrated(discount.getConcentrated() + valor);
            } else if ("COMISION BANCO".equals(typeDiscount)) {
                discount.setCommission(discount.getCommission() + valor);
            } else if ("YOGURT".equals(typeDiscount)) {
                discount.setYogurt(discount.getYogurt() + valor);
            } else if ("VETERINARIO".equals(typeDiscount)) {
                discount.setVeterinary(discount.getVeterinary() + valor);
            } else if ("TACHOS".equals(typeDiscount)) {
                discount.setCans(discount.getCans() + valor);
            } else if ("OTROS EGRESOS".equals(typeDiscount)) {
                discount.setOtherDiscount(discount.getOtherDiscount() + valor);
            } else if ("OTROS INGRESOS".equals(typeDiscount)) {
                discount.setOtherIncoming(discount.getOtherIncoming() + valor);
            } else if ("CREDITO".equals(typeDiscount)) {
                discount.setCredit(discount.getCredit() + valor);
            }
        }

        // Redondear valores finales
        for (RawMaterialProducerDiscount discount : result.values()) {
            discount.setConcentrated(RoundUtil.getRoundValue(discount.getConcentrated(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setCommission(RoundUtil.getRoundValue(discount.getCommission(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setYogurt(RoundUtil.getRoundValue(discount.getYogurt(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setVeterinary(RoundUtil.getRoundValue(discount.getVeterinary(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setCredit(RoundUtil.getRoundValue(discount.getCredit(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setCans(RoundUtil.getRoundValue(discount.getCans(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setOtherDiscount(RoundUtil.getRoundValue(discount.getOtherDiscount(), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setOtherIncoming(RoundUtil.getRoundValue(discount.getOtherIncoming(), 2, RoundUtil.RoundMode.SYMMETRIC));
        }

        return result;
    }

    @Override
    public List<SalaryMovementProducer> findFiltered(Date startDate, Date endDate, TypeMovementProducer typeMovementProducer, String firstName, String lastName, String maidenName) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT salaryMovementProducer FROM SalaryMovementProducer salaryMovementProducer");
        jpql.append(" LEFT JOIN FETCH salaryMovementProducer.rawMaterialProducer rawMaterialProducer");
        jpql.append(" LEFT JOIN FETCH salaryMovementProducer.typeMovementProducer typeMovementProducer");
        jpql.append(" LEFT JOIN FETCH salaryMovementProducer.productiveZone productiveZone");
        jpql.append(" LEFT JOIN FETCH salaryMovementProducer.company company");
        jpql.append(" WHERE 1=1");

        if (startDate != null) {
            jpql.append(" AND salaryMovementProducer.date >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" AND salaryMovementProducer.date <= :endDate");
        }
        if (typeMovementProducer != null) {
            jpql.append(" AND salaryMovementProducer.typeMovementProducer = :typeMovementProducer");
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            jpql.append(" AND upper(rawMaterialProducer.firstName) LIKE :firstName");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            jpql.append(" AND upper(rawMaterialProducer.lastName) LIKE :lastName");
        }
        if (maidenName != null && !maidenName.trim().isEmpty()) {
            jpql.append(" AND upper(rawMaterialProducer.maidenName) LIKE :maidenName");
        }
        jpql.append(" ORDER BY salaryMovementProducer.date DESC");

        javax.persistence.Query query = em.createQuery(jpql.toString());

        if (startDate != null) {
            query.setParameter("startDate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate, TemporalType.DATE);
        }
        if (typeMovementProducer != null) {
            query.setParameter("typeMovementProducer", typeMovementProducer);
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            query.setParameter("firstName", "%" + firstName.trim().toUpperCase() + "%");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            query.setParameter("lastName", "%" + lastName.trim().toUpperCase() + "%");
        }
        if (maidenName != null && !maidenName.trim().isEmpty()) {
            query.setParameter("maidenName", "%" + maidenName.trim().toUpperCase() + "%");
        }

        return query.getResultList();
    }

    /** Tipos de descuento (movimiento por productor) que ARRASTRAN deuda. */
    private static final java.util.List<String> CARRY_TYPES = java.util.Arrays.asList(
            "VETERINARIO", "CREDITO", "CONCENTRADOS", "YOGURT", "TACHOS", "OTROS EGRESOS");

    /** Descuento prioritario que NO arrastra, pero se aplica por movimiento (con trazabilidad)
     *  para que el saldo baje y quede PAGADO solo si realmente se cobro. */
    private static final java.util.List<String> COMMISSION_TYPES = java.util.Arrays.asList("COMISION BANCO");

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, List<SalaryMovementProducer>> preloadCommissionMovements(Date startDate, Date endDate, ProductiveZone productiveZone) {
        // Solo los de la quincena (no arrastra), con saldo>0, por productor y en orden FIFO.
        List<SalaryMovementProducer> list = getEntityManager().createQuery(
                "select m from SalaryMovementProducer m " +
                " join fetch m.typeMovementProducer t " +
                " where m.saldo > 0 " +
                " and m.date between :startDate and :endDate " +
                " and m.productiveZone = :productiveZone " +
                " and t.name in (:types) " +
                " order by m.rawMaterialProducer.id asc, m.date asc, m.id asc")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("productiveZone", productiveZone)
                .setParameter("types", COMMISSION_TYPES)
                .getResultList();

        Map<Long, List<SalaryMovementProducer>> result = new java.util.HashMap<Long, List<SalaryMovementProducer>>();
        for (SalaryMovementProducer m : list) {
            Long producerId = m.getRawMaterialProducer().getId();
            List<SalaryMovementProducer> l = result.get(producerId);
            if (l == null) {
                l = new ArrayList<SalaryMovementProducer>();
                result.put(producerId, l);
            }
            l.add(m);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, List<SalaryMovementProducer>> preloadCarryMovements(Date endDate, ProductiveZone productiveZone) {
        List<SalaryMovementProducer> list = getEntityManager().createQuery(
                "select m from SalaryMovementProducer m " +
                " join fetch m.typeMovementProducer t " +
                " where m.saldo > 0 " +
                " and m.date <= :endDate " +
                " and m.productiveZone = :productiveZone " +
                " and t.name in (:types) " +
                " order by m.rawMaterialProducer.id asc, m.date asc, m.id asc")
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("productiveZone", productiveZone)
                .setParameter("types", CARRY_TYPES)
                .getResultList();

        Map<Long, List<SalaryMovementProducer>> result = new java.util.HashMap<Long, List<SalaryMovementProducer>>();
        for (SalaryMovementProducer m : list) {
            Long producerId = m.getRawMaterialProducer().getId();
            List<SalaryMovementProducer> l = result.get(producerId);
            if (l == null) {
                l = new ArrayList<SalaryMovementProducer>();
                result.put(producerId, l);
            }
            l.add(m);
        }
        return result;
    }

    @Override
    public void importSalaryMovements(List<SalaryMovementProducer> list) {
        for (SalaryMovementProducer salaryMovementProducer : list) {
            em.persist(salaryMovementProducer);
        }
        em.flush();
    }

    @Override
    public List<SalaryMovementProducer> findSalaryMovementProducerList(Date startDate, Date endDate, TypeMovementProducer typeMovementProducer) {

        List<SalaryMovementProducer> salaryMovementProducerList = em.createQuery("" +
                "select s from SalaryMovementProducer s " +
                " where s.date between :startDate and :endDate " +
                " and s.typeMovementProducer =:typeMovementProducer ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("typeMovementProducer", typeMovementProducer)
                .getResultList();

        if (salaryMovementProducerList == null)
            salaryMovementProducerList = new ArrayList<SalaryMovementProducer>();

        return salaryMovementProducerList;
    }

}
