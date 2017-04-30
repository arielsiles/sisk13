package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.RoundUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
@Name("indirectCostsService")
@AutoCreate
@Stateless
public class IndirectCostsServiceBean extends ExtendedGenericServiceBean implements IndirectCostsService {


    //Calendar mycal = new GregorianCalendar(DateUtils.getCurrentYear(new Date()), Calendar.MONTH -3, 1);
    //todo: se tomara el mes actual para regularizar Enero ... Marzo
    //Calendar calendar = Calendar.getInstance();
    int daysInMonth;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public Double getTotalCostIndirectGeneral() {
        BigDecimal total = new BigDecimal(0.0);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.MONTH, -1);

        try {

            total = (BigDecimal) em.createQuery("SELECT sum(indirectCosts.amountBs) from IndirectCostsConifg indirectCosts" +
                    " where indirectCosts.month = :month and indirectCosts.year = :year and indirectCosts.type = :type")
                    .setParameter("month", DateUtils.getCurrentMonth(calendar.getTime()))
                    .setParameter("year", DateUtils.getCurrentYear(calendar.getTime()))
                    .setParameter("type", Constants.INDIRECT_COST_TYPE_GENERAL)
                    .getSingleResult();

        } catch (NoResultException e) {
            total = new BigDecimal(0.0);
        }
        return total.doubleValue() / daysInMonth;
    }

    public List<IndirectCosts> getIndirectCostGeneral(PeriodIndirectCost indirectCost)
    {
        List<IndirectCosts> indirectCostsList = new ArrayList<IndirectCosts>();

        try{
            indirectCostsList = em.createQuery("select indirectCosts from IndirectCosts indirectCosts " +
                                               " inner join indirectCosts.costsConifg costsConifg " +
                                               " where indirectCosts.periodIndirectCost = :periodIndirectCost")
                                  .setParameter("periodIndirectCost",indirectCost)
                                  .getResultList();

        } catch(NoResultException e){
            return new ArrayList<IndirectCosts>();
        }

        return indirectCostsList;
    }

    @Override
    public boolean findPeriodIndirectCostUsed(PeriodIndirectCost periodIndirectCost) {
        Date startDate = DateUtils.firstDayOfMonth(periodIndirectCost.getMonth(),periodIndirectCost.getGestion().getYear());
        Date endDate = DateUtils.lastDayOfMonth(periodIndirectCost.getMonth(),periodIndirectCost.getGestion().getYear());
        List<ProductionPlanning> productionPlannings = new ArrayList<ProductionPlanning>();
        try{
            productionPlannings = (List<ProductionPlanning>)em.createQuery(" select productionPlanning from ProductionPlanning productionPlanning " +
                                                                " where productionPlanning.date between :startDate and :endDate " +
                                                                " and productionPlanning.state = :state")
                                                   .setParameter("startDate",startDate,TemporalType.DATE)
                                                   .setParameter("endDate",endDate,TemporalType.DATE)
                                                   .setParameter("state",ProductionPlanningState.INSTOCK)
                                                   .getResultList();
        }catch (NoResultException e)
        {
            return false;
        }
        if(productionPlannings == null)
            return false;

        return productionPlannings.size() > 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<IndirectCosts> getIndirectCostByGroup(PeriodIndirectCost indirectCost, SubGroup subGroup)
    {
        List<IndirectCosts> indirectCostsList = new ArrayList<IndirectCosts>();

        try{
            indirectCostsList = em.createQuery("select indirectCosts from IndirectCosts indirectCosts " +
                    " inner join indirectCosts.costsConifg costsConifg " +
                    " where indirectCosts.periodIndirectCost = :periodIndirectCost" +
                    " and indirectCosts.costsConifg.group = :group")
                    .setParameter("group",subGroup.getGroup())
                    .setParameter("periodIndirectCost", indirectCost)
                    .getResultList();

        } catch(NoResultException e){
            return new ArrayList<IndirectCosts>();
        }

        return indirectCostsList;
    }

    @Override
    public List<IndirectCosts> getCostTotalIndirect(Date dateConcurrent, int totalDaysNotProducer,ProductionOrder productionOrder, Double totalVolumDay, Double totalVolumGeneralDay, PeriodIndirectCost periodIndirectCost) {

        List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        indirectCostses.addAll(generateCosts(totalDaysNotProducer,periodIndirectCost, productionOrder, totalVolumGeneralDay, getIndirectCostGeneral(periodIndirectCost)));
        indirectCostses.addAll(generateCosts(totalDaysNotProducer,periodIndirectCost, productionOrder, totalVolumDay, getIndirectCostByGroup(periodIndirectCost, productionOrder.getProductMain() == null ? productionOrder.getProductComposition().getProcessedProduct().getProductItem().getSubGroup() : productionOrder.getProductOrders().get(0).getProcessedProduct().getProductItem().getSubGroup() )));

        /*Double totalVolumeOrder = getTotalVolumeOrder(productionOrder);
        Double totalCostIndirectGeneral = getTotalCostIndirectGeneral();
        Double totalCostIndirectByGroup = getTotalCostIndirectByGroup(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getSubGroup());
        Double costGeneral = totalCostIndirectGeneral * getPorcent(totalVolumGeneralDay, totalVolumeOrder) / 100;
        Double costByGroup = totalCostIndirectByGroup * getPorcent(totalVolumDay, totalVolumeOrder) / 100;*/

        return indirectCostses;
    }

    @Override
    public List<IndirectCosts> getCostTotalIndirectSingle(Date dateConcurrent, int totalDaysNotProducer,SingleProduct single, Double totalVolumDay, Double totalVolumGeneralDay, PeriodIndirectCost periodIndirectCost) {

        List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        indirectCostses.addAll(generateCosts(totalDaysNotProducer,periodIndirectCost, single, totalVolumGeneralDay, getIndirectCostGeneral(periodIndirectCost)));
        indirectCostses.addAll(generateCosts(totalDaysNotProducer,periodIndirectCost, single, totalVolumDay, getIndirectCostByGroup(periodIndirectCost, single.getProductProcessingSingle().getMetaProduct().getProductItem().getSubGroup())));

        /*Double totalVolumeOrder = getTotalVolumeOrder(productionOrder);
        Double totalCostIndirectGeneral = getTotalCostIndirectGeneral();
        Double totalCostIndirectByGroup = getTotalCostIndirectByGroup(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getSubGroup());
        Double costGeneral = totalCostIndirectGeneral * getPorcent(totalVolumGeneralDay, totalVolumeOrder) / 100;
        Double costByGroup = totalCostIndirectByGroup * getPorcent(totalVolumDay, totalVolumeOrder) / 100;*/

        return indirectCostses;
    }

    @Override
    public PeriodIndirectCost getLastPeroidIndirectCost()
    {
        List<PeriodIndirectCost> periodIndirectCosts = new ArrayList<PeriodIndirectCost>();

        try{

            periodIndirectCosts = em.createQuery("select periodIndirectCost from PeriodIndirectCost periodIndirectCost " +
                                                 "order by periodIndirectCost.id desc")
                                                .getResultList();
        }catch (NoResultException e){
            return null;
        }

        return periodIndirectCosts.get(0);
    }

    private List<IndirectCosts> generateCosts(int totalDaysNotProducer, PeriodIndirectCost periodIndirectCost,ProductionOrder productionOrder, Double totalVolumDay, List<IndirectCosts> costses)
    {
        List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();
        for(IndirectCosts costs: costses)
        {
            IndirectCosts aux = new IndirectCosts();
            aux.setName(costs.getName());
            aux.setCompany(costs.getCompany());
            aux.setCostsConifg(costs.getCostsConifg());
            Double totalVolumeOrder = getTotalVolumeOrder(productionOrder);
            Double amountCost = (costs.getAmountBs().doubleValue() / daysInMonth);
            Double costGeneral = amountCost * getPorcent(totalVolumDay, totalVolumeOrder) / 100;
            aux.setAmountBs(new BigDecimal(RoundUtil.getRoundValue(costGeneral * totalDaysNotProducer,2, RoundUtil.RoundMode.SYMMETRIC)));
            indirectCostses.add(aux);
        }
        return indirectCostses;
    }

    private List<IndirectCosts> generateCosts(int totalDaysNotProducer, PeriodIndirectCost periodIndirectCost,SingleProduct product, Double totalVolumDay, List<IndirectCosts> costses)
    {
        List<IndirectCosts> indirectCostses = new ArrayList<IndirectCosts>();
        for(IndirectCosts costs: costses)
        {
            IndirectCosts aux = new IndirectCosts();
            aux.setName(costs.getName());
            aux.setCompany(costs.getCompany());
            aux.setCostsConifg(costs.getCostsConifg());
            Double totalVolumeOrder = getTotalVolumeSingle(product);
            Double amountCost = costs.getAmountBs().doubleValue() / daysInMonth;
            Double costGeneral = amountCost * getPorcent(totalVolumDay, totalVolumeOrder) / 100;
            aux.setAmountBs(new BigDecimal(RoundUtil.getRoundValue(costGeneral * totalDaysNotProducer,2, RoundUtil.RoundMode.SYMMETRIC)));
            indirectCostses.add(aux);
        }
        return indirectCostses;
    }

    /*public Double getTotalCostIndirectByAccount(CashAccount cashAccount) {
        BigDecimal total = new BigDecimal(0.0);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.MONTH, -1);

        try {

            total = (BigDecimal) em.createQuery("SELECT sum(indirectCosts.amountBs) from IndirectCostsConfig indirectCosts" +
                    " where indirectCosts.month = :month and indirectCosts.year = :year and indirectCosts.cashAccount = :cashAccount")
                    .setParameter("month", DateUtils.getCurrentMonth(calendar.getTime()))
                    .setParameter("year", DateUtils.getCurrentYear(calendar.getTime()))
                    .setParameter("cashAccount", cashAccount)
                    .getSingleResult();

        } catch (NoResultException e) {
            total = new BigDecimal(0.0);
        }
        return total.doubleValue() / 30;
    }
*/
  /*  public Double getTotalCostIndirectGeneral(String type) {
        BigDecimal total = new BigDecimal(0.0);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.MONTH, -1);

        try {

            total = (BigDecimal) em.createQuery("SELECT sum(indirectCosts.amountBs) from IndirectCostsConfig indirectCosts" +
                    " where indirectCosts.month = :month and indirectCosts.year = :year and indirectCosts.type = :type")
                    .setParameter("month", DateUtils.getCurrentMonth(calendar.getTime()))
                    .setParameter("year", DateUtils.getCurrentYear(calendar.getTime()))
                    .setParameter("type", type)
                    .getSingleResult();

        } catch (NoResultException e) {
            total = new BigDecimal(0.0);
        }
        return total.doubleValue() / 30;
    }*/

    public Double getPorcent(Double totalDay, Double totalOrder) {
        if (totalDay == 0.0)
            return 0.0;

        return (totalOrder * 100) / totalDay;
    }

    @Override
    public Double getCostTotalIndirect(ProductionOrder productionOrder, Double totalVolumDay, Double totalVolumGeneralDay) {
        Double totalCost = 0.0;

        Double totalVolumeOrder = getTotalVolumeOrder(productionOrder);
        Double totalCostIndirectGeneral = getTotalCostIndirectGeneral();
        Double totalCostIndirectByGroup = getTotalCostIndirectByGroup(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getSubGroup());
        Double costGeneral = totalCostIndirectGeneral * getPorcent(totalVolumGeneralDay, totalVolumeOrder) / 100;
        Double costByGroup = totalCostIndirectByGroup * getPorcent(totalVolumDay, totalVolumeOrder) / 100;
        totalCost = costGeneral + costByGroup;
        return totalCost;
    }

    @Override
    public Double getTotalCostIndirectByGroup(SubGroup subGroup) {
        BigDecimal total = new BigDecimal(0.0);
        try {

            total = (BigDecimal) em.createQuery("SELECT sum(indirectCosts.amountBs) from IndirectCostsConifg indirectCosts" +
                    " where indirectCosts.month = :month and indirectCosts.year = :year and indirectCosts.group = :group ")
                    //" where indirectCosts.month = :month and indirectCosts.year = :year and indirectCosts.group.id = :id ")
                    //.setParameter("id",subGroup.getGroupCode())
                    //.setParameter("id","8")
                    .setParameter("group", subGroup.getGroup())
                    .setParameter("month", DateUtils.getCurrentMonth(new Date()) - 1)
                    .setParameter("year", DateUtils.getCurrentYear(new Date()))
                    .getSingleResult();


        } catch (NoResultException e) {
            total = new BigDecimal(0.0);
        }
        if (total == null )
            total = new BigDecimal(0.0);

        return total.doubleValue() / daysInMonth;
    }
    //TODO: se utilizara la cantidad esperada en lugar de la producida
    public Double getTotalVolumeOrder(ProductionOrder productionOrder) {
        //Double total = productionOrder.getProducedAmount();
        Double total = productionOrder.getExpendAmount();
        Double amount = 0.0;
        ProcessedProduct processedProduct;
        if(productionOrder.getProductMain() != null)
            processedProduct = productionOrder.getProductOrders().get(0).getProcessedProduct();
        else
            processedProduct = productionOrder.getProductComposition().getProcessedProduct();

        String unitMeasure = processedProduct.getUnidMeasure();

        if (processedProduct.getAmount() != null)
            amount = processedProduct.getAmount();

        if (unitMeasure == "KG" || unitMeasure == "LT")
            amount = processedProduct.getAmount() * 1000;

        total = amount * productionOrder.getProducedAmount();
        //total = amount * productionOrder.getExpendAmount();
        return total;
    }

    //todo: en caso que el producto procesado no cuente con con un monto lanzar un mensaje de alerta

    public Double getTotalVolumeSingle(SingleProduct single){
        Double total = single.getAmount().doubleValue();
        ProcessedProduct product = getProductProcessingByMetaProduct(single.getProductProcessingSingle().getMetaProduct());
        String unitMeasure = product.getUnidMeasure();
        Double amount = 0.0;

        if (product.getAmount() != null)
            amount = product.getAmount();

        if (unitMeasure == "KG" || unitMeasure == "LT")
            amount = product.getAmount() * 1000;

        total = amount * single.getAmount();
        return total;
    }

    public ProcessedProduct getProductProcessingByMetaProduct(MetaProduct metaProduct)
    {
        ProcessedProduct processedProduct;

        try{

            processedProduct = (ProcessedProduct) getEntityManager().createQuery("SELECT processedProduct from ProcessedProduct processedProduct where processedProduct = :metaProduct ")
                    .setParameter("metaProduct",metaProduct)
                    .getSingleResult();

        }catch (NoResultException e)
        {
            return null;
        }

        return processedProduct;
    }
    //Todo: muy importante este metodo toma la fecha de la orden para obtener los dias no producidos
    public int calculateCantDaysProducer(Date date){
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        int totalDays = 1;


        int day = calender.get(Calendar.DAY_OF_MONTH) - 1;
        for(int i= day; i >= 1;i--)
        {
            try{
            calender.set(Calendar.DAY_OF_MONTH,i);
            ProductionPlanning planning =  (ProductionPlanning)getEntityManager().createQuery(" select productionPlanning from ProductionPlanning productionPlanning " +
                    " where productionPlanning.date = :date ")
                    .setParameter("date",calender, TemporalType.DATE)
                    .getSingleResult();

                return totalDays;
            }catch(NoResultException e){
                totalDays ++;
            }
        }

        return totalDays;
    }

    @Override
    public PeriodIndirectCost getConcurrentPeroidIndirectCost(Date dateConcurrent) {

        PeriodIndirectCost periodIndirectCost = new PeriodIndirectCost();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateConcurrent);


        try{
            Object[] datas =  (Object[])em.createNativeQuery("select pe.*\n" +
                    "from PERIODOCOSTOINDIRECTO pe \n" +
                    "inner join gestion ge\n" +
                    "on pe.idgestion = ge.idgestion \n" +
                    "where pe.MES= :month and ge.anio= :year ")
                    //todo: se suma uno al mes para igualar con el periodo ya que el mes va 0-11
                    .setParameter("month",calendar.get(Calendar.MONTH)+1)
                    .setParameter("year",calendar.get(Calendar.YEAR))
                    .getSingleResult();
            /*periodIndirectCost.setId(((BigDecimal)datas[0]).longValue());
            periodIndirectCost.setMonth(((BigDecimal)datas[1]).intValue());*/
            // Changes to Mysql:
            periodIndirectCost.setId(((BigInteger)datas[0]).longValue());
            periodIndirectCost.setMonth(((Integer)datas[1]).intValue());

        }catch (NoResultException e){
            List<Object[]> datas =  (List<Object[]>)em.createNativeQuery("select pe.*\n" +
                    "from PERIODOCOSTOINDIRECTO pe \n" +
                    "inner join gestion ge\n" +
                    "on pe.idgestion = ge.idgestion ")
                    .getResultList();
            Object[] data = datas.get(0);
            /*periodIndirectCost.setId(((BigDecimal)data[0]).longValue());
            periodIndirectCost.setMonth(((BigDecimal)data[1]).intValue());*/
            // Changes to Mysql:
            periodIndirectCost.setId(((BigInteger)data[0]).longValue());
            periodIndirectCost.setMonth(((Integer)data[1]).intValue());
        }

        return periodIndirectCost;
    }
}
