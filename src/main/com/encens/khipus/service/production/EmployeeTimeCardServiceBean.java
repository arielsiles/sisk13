package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ariel Siles Encias
 */

@Stateless
@Name("employeeTimeCardService")
@AutoCreate
public class EmployeeTimeCardServiceBean extends GenericServiceBean implements EmployeeTimeCardService {

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private JobContractService jobContractService;

    public BigDecimal costProductionOrder(ProductionOrder productionOrder) {

        ArrayList employeeTimeCardList = new ArrayList<EmployeeTimeCard>();
        //employeeTimeCardList = (ArrayList<EmployeeTimeCard>) em.createNamedQuery("EmployeeTimeCard.findEmployeeTimeCardByProductionOrder").setParameter("productionOrder", productionOrder).getResultList();
        employeeTimeCardList = (ArrayList<EmployeeTimeCard>) em.createQuery("SELECT employeeTimeCard from EmployeeTimeCard employeeTimeCard").getResultList();
        double totalMinutes = 0;
        double totalCost = 0;

        for (int i = 0; i < employeeTimeCardList.size(); i++) {
            Employee employee = ((EmployeeTimeCard) employeeTimeCardList.get(i)).getEmployee();
            Date startTime = ((EmployeeTimeCard) employeeTimeCardList.get(i)).getStartTime();
            Date endTime = ((EmployeeTimeCard) employeeTimeCardList.get(i)).getEndTime();

            long diffHours = DateUtils.differenceBetween(startTime, endTime, TimeUnit.HOURS);
            diffHours = diffHours - 1;
            long diffMinutes = DateUtils.differenceBetween(startTime, endTime, TimeUnit.MINUTES);
            diffMinutes = diffMinutes - 1;
            totalMinutes = totalMinutes + diffMinutes;
            JobContract jobContract = jobContractService.lastJobContractByEmployee(employee);
            double basicMinute = ((jobContract.getJob().getSalary().getBasicAmount().doubleValue() / 30) / 8) / 60;
            totalCost = totalCost + (diffMinutes * basicMinute);
        }

        return new BigDecimal(totalCost);
    }

    public List<EmployeeTimeCard> getEmployeesWorkingInDay(Date dateOrder, ProductionOrder productionOrder) {
        List<EmployeeTimeCard> timeCards = new ArrayList<EmployeeTimeCard>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateOrder.getTime());
        calendar.add(Calendar.DATE, 1);
        try {
            timeCards = em.createQuery("SELECT employeeTimeCard from EmployeeTimeCard employeeTimeCard " +
                    "where employeeTimeCard.subGroup =:subGroup " +
                    "and employeeTimeCard.date between  :dateIni and :endDate " +
                    "order by employeeTimeCard.employee.id ")
                    .setParameter("dateIni", dateOrder, TemporalType.DATE)
                    .setParameter("endDate", calendar.getTime(), TemporalType.DATE)
                    .setParameter("subGroup", productionOrder.getProductComposition().getProcessedProduct().getProductItem().getSubGroup())
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }

        return timeCards;
    }

    private Double getCostDayBySubGroup(List<EmployeeTimeCard> timeCards) {

        List<Employee> notTake = new ArrayList<Employee>();
        double totalMinutes = 0;
        double totalCost = 0;
        Boolean band = true;

        for (EmployeeTimeCard employeeTimeCard : timeCards) {

            Date startTime = employeeTimeCard.getStartTime();
            Date endTime = employeeTimeCard.getEndTime();


            if (!notTake.contains(employeeTimeCard.getEmployee()) && endTime != null) {
                long diffHours = DateUtils.differenceBetween(startTime, endTime, TimeUnit.HOURS);
                diffHours = diffHours - 1;
                long diffMinutes = DateUtils.differenceBetween(startTime, endTime, TimeUnit.MINUTES);
                diffMinutes = diffMinutes - 1;
                totalMinutes = totalMinutes + diffMinutes;
                JobContract jobContract = jobContractService.lastJobContractByEmployee(employeeTimeCard.getEmployee());
                double basicMinute = ((jobContract.getJob().getSalary().getBasicAmount().doubleValue() / 30) / 8) / 60;
                totalCost = totalCost + (diffMinutes * basicMinute);
            }

            if (employeeTimeCard.getEndDay() != null) {
                band = false;
                notTake.add(employeeTimeCard.getEmployee());
            }

        }

        return totalCost;
    }

    public Double getPorcent(Double totalDay, Double totalOrder) {
        if (totalDay == 0.0)
            return 0.0;

        return (totalOrder * 100) / totalDay;
    }

    @Override
    public BigDecimal getCostProductionOrder(ProductionOrder productionOrder, Date dateOrder, Double totalVolumDay) {

        List<EmployeeTimeCard> timeCards = new ArrayList<EmployeeTimeCard>();
        Double totalCost = 0.0;
        timeCards = getEmployeesWorkingInDay(dateOrder, productionOrder);
        Double costDayBySubGroup = getCostDayBySubGroup(timeCards);
        Double totalVolumeOrder = getTotalVolumeOrder(productionOrder);

        totalCost = costDayBySubGroup * getPorcent(totalVolumDay, totalVolumeOrder) / 100;

        return new BigDecimal(totalCost);
    }

    //todo: se tomara el monto esperado para hacer el calculo del volumen total
    @Override
    public Double getTotalVolumeOrder(ProductionOrder productionOrder) {
        Double total = productionOrder.getProducedAmount();
        String unitMeasure ;
        ProcessedProduct processedProduct;
        if(productionOrder.getProductMain() == null) {
            processedProduct = productionOrder.getProductComposition().getProcessedProduct();
            unitMeasure = processedProduct.getUnidMeasure();
        }
        else {
            processedProduct = productionOrder.getProductOrders().get(0).getProcessedProduct();
            unitMeasure = processedProduct.getUnidMeasure();
        }
        Double amount = 0.0;
        if (processedProduct.getAmount() != null)
            amount = processedProduct.getAmount();

        if (unitMeasure == "KG" || unitMeasure == "LT")
            amount = processedProduct.getAmount() * 1000;

        total = amount * productionOrder.getProducedAmount();
        return total;
    }
    //todo: en caso que el producto procesado no cuente con con un monto lanzar un mensaje de alerta
    @Override
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

    @Override
    public List<EmployeeTimeCard> getLastTimesCards() {
        List<EmployeeTimeCard> timeCards = new ArrayList<EmployeeTimeCard>();
        try {
            timeCards = (List<EmployeeTimeCard>) em.createQuery("SELECT employeeTimeCard from EmployeeTimeCard EmployeeTimeCard order by employeeTimeCard.startTime desc")
                    .setMaxResults(8)
                    .getResultList();

        } catch (NoResultException e) {
            return new ArrayList<EmployeeTimeCard>();
        }

        return timeCards;
    }

    @Override
    public List<EmployeeTimeCard> getLastTimesCardsEmployee(Employee employeeSelect) {
        List<EmployeeTimeCard> timeCards = new ArrayList<EmployeeTimeCard>();
        try {
            if (employeeSelect != null)
                timeCards = (List<EmployeeTimeCard>) em.createQuery("SELECT employeeTimeCard from EmployeeTimeCard EmployeeTimeCard " +
                        " where employeeTimeCard.employee = :employeeSelect " +
                        " order by employeeTimeCard.startTime desc")
                        .setParameter("employeeSelect", employeeSelect)
                        .setMaxResults(8)
                        .getResultList();

        } catch (NoResultException e) {
            return new ArrayList<EmployeeTimeCard>();
        }

        return timeCards;
    }

    @Override
    public Double getCostPerHour(Employee employee) {

        JobContract jobContract = jobContractService.lastJobContractByEmployee(employee);
        if(jobContract == null)
            return 0.0;

        Double costPerHour = ((jobContract.getJob().getSalary().getBasicAmount().doubleValue() / 30) / 8);

        return costPerHour;
    }

    @Override
    public List<ProductionTaskType> getTaskTypeGroup(Group group) {
        List<ProductionTaskType> taskTypes = new ArrayList<ProductionTaskType>();
        try {
            taskTypes = em.createQuery("SELECT productionTaskType FROM ProductionTaskType productionTaskType WHERE productionTaskType.group = :grupo")
                    .setParameter("grupo", group)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<ProductionTaskType>();
        }

        return taskTypes;
    }

    @Override
    public List<ConfigGroup> getConfigGroupsProduction() {
        List<ConfigGroup> groups = new ArrayList<ConfigGroup>();
        try {
            groups = em.createQuery("SELECT configGroup FROM ConfigGroup configGroup WHERE configGroup.type = :type")
                    .setParameter("type", "AREA_PRODUCTOS")
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<ConfigGroup>();
        }

        return groups;
    }

    @Override
    public List<ProductionTaskType> getTaskType() {
        List<ProductionTaskType> taskTypes = new ArrayList<ProductionTaskType>();
        try {
            taskTypes = em.createQuery("SELECT productionTaskType FROM ProductionTaskType productionTaskType")
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<ProductionTaskType>();
        }

        return taskTypes;
    }

    @Override
    public Date getLastMark(Employee employeeSelect) {
        Date dateRegister = new Date();
        try {

            List<EmployeeTimeCard> resultList = em.createQuery("SELECT employeeTimeCard FROM EmployeeTimeCard employeeTimeCard WHERE employeeTimeCard.employee = :employee order by employeeTimeCard.endTime desc")
                    .setParameter("employee", employeeSelect)
                    .getResultList();
            if (resultList.get(0).getProductionTaskType().getName().compareTo("FINALIZADO") != 0)
                dateRegister = resultList.get(0).getEndTime();

        } catch (NoResultException e) {
            return new Date();
        }

        return dateRegister;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EmployeeTimeCard getLastEmployeeTimeCard(Employee employeeSelect) {
        EmployeeTimeCard employeeTimeCard = null;
        try {

            List<EmployeeTimeCard> resultList = em.createQuery("SELECT employeeTimeCard FROM EmployeeTimeCard employeeTimeCard WHERE employeeTimeCard.employee = :employee order by employeeTimeCard.startTime desc")
                    .setParameter("employee", employeeSelect)
                    .getResultList();
            if (resultList.size() > 0)
                if (resultList.get(0).getProductionTaskType().getName().compareTo(Constants.EMPLOYEE_CARD_FINALIZE) != 0)
                    employeeTimeCard = resultList.get(0);

        } catch (NoResultException e) {
            return null;
        }

        return employeeTimeCard;
    }


}
