package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 3.2
 */
@Stateless
@Name("christmasPayrollService")
@AutoCreate
public class ChristmasPayrollServiceBean extends GenericServiceBean implements ChristmasPayrollService {

    @In("#{entityManager}")
    private EntityManager em;

    public ChristmasPayroll buildChristmasPayroll(GeneratedPayroll generatedPayroll, GestionPayroll gestionPayroll,
                                                  Employee employee, Date initContractDate, BigDecimal salary,
                                                  int workedDays, ManagersPayroll novemberManagersPayroll,
                                                  BigDecimal septemberTotalIncome, BigDecimal octoberTotalIncome,
                                                  BigDecimal novemberTotalIncome, BigDecimal averageSalary,
                                                  BigDecimal contributableSalary, BankAccount bankAccount) {
        ChristmasPayroll christmasPayroll = new ChristmasPayroll();
        christmasPayroll.setEmployee(employee);
        christmasPayroll.setContractInitDate(initContractDate);
        christmasPayroll.setWorkedDays(BigDecimalUtil.toBigDecimal(workedDays));
        christmasPayroll.setSalary(salary);
        christmasPayroll.setSeptemberTotalIncome(septemberTotalIncome);
        christmasPayroll.setOctoberTotalIncome(octoberTotalIncome);
        christmasPayroll.setNovemberTotalIncome(novemberTotalIncome);
        christmasPayroll.setAverageSalary(averageSalary);
        christmasPayroll.setContributableSalary(contributableSalary);
        christmasPayroll.setLiquid(contributableSalary);
        christmasPayroll.setGeneratedPayroll(generatedPayroll);
        christmasPayroll.setBusinessUnit(novemberManagersPayroll.getBusinessUnit());
        christmasPayroll.setCostCenter(novemberManagersPayroll.getCostCenter());
        christmasPayroll.setArea(novemberManagersPayroll.getArea());
        christmasPayroll.setCharge(novemberManagersPayroll.getCharge());
        christmasPayroll.setJobCategory(gestionPayroll.getJobCategory());
        if (null != bankAccount) {
            christmasPayroll.setBankAccount(bankAccount.getAccountNumber());
            christmasPayroll.setBankAccountCurrency(bankAccount.getCurrency().getSymbol());
            christmasPayroll.setClientCode(bankAccount.getClientCod());
        }
        return christmasPayroll;
    }


    public void insertChristmasPayroll(ChristmasPayroll christmasPayroll){

        System.out.println("--------------------");
        System.out.println("initDate..." + christmasPayroll.getContractInitDate());
        System.out.println("employeeId..." + christmasPayroll.getEmployee().getId());
        System.out.println("workedDays..." + christmasPayroll.getWorkedDays());
        System.out.println("salary..." + christmasPayroll.getSalary());
        System.out.println("septemberTotalIncome..." + christmasPayroll.getSeptemberTotalIncome());
        System.out.println("octoberTotalIncome..." + christmasPayroll.getOctoberTotalIncome());
        System.out.println("novemberTotalIncome..." + christmasPayroll.getNovemberTotalIncome());
        System.out.println("averageSalary..." + christmasPayroll.getAverageSalary());
        System.out.println("contributableSalary..." + christmasPayroll.getContributableSalary());
        System.out.println("liquid..." + christmasPayroll.getLiquid());
        System.out.println("generatedPayrollId..." + christmasPayroll.getGeneratedPayroll().getId());
        System.out.println("businessUnitId..." + christmasPayroll.getBusinessUnit().getId());
        System.out.println("costCenter..." + christmasPayroll.getCostCenterCode());
        System.out.println("area..." + christmasPayroll.getArea());
        System.out.println("chargeId..." + christmasPayroll.getCharge().getId());
        System.out.println("jobCategoryId..." + christmasPayroll.getJobCategory().getId());
        System.out.println("bankAccount..." + christmasPayroll.getBankAccount());


        em.createNativeQuery("insert into planillaaguinaldo (fechainiciocontrato,idempleado,diastrabajados,sueldo,totalganadoseptiembre,totalganadooctubre,totalganadonoviembre," +
                "sueldopromedio,sueldocotizable,liquidopagable,idplanillagenerada,idunidadnegocio, codigocencos, area, idcargo, idcategoriapuesto, cuentabancaria, monedactabancaria, codigocliente, version, idcompania) " +
                "values(:initDate,:employeeId,:workedDays,:salary,:septemberTotalIncome,:octoberTotalIncome,:novemberTotalIncome," +
                ":averageSalary,:contributableSalary,:liquid,:generatedPayrollId,:businessUnitId,:costCenter,:area,:chargeId,:jobCategoryId,:bankAccount,:bankAccountCurrency,:clientCode,:version, :companyId)")
                .setParameter("initDate", christmasPayroll.getContractInitDate())
                .setParameter("employeeId", christmasPayroll.getEmployee().getId())
                .setParameter("workedDays", christmasPayroll.getWorkedDays())
                .setParameter("salary", christmasPayroll.getSalary())
                .setParameter("septemberTotalIncome", christmasPayroll.getSeptemberTotalIncome())
                .setParameter("octoberTotalIncome", christmasPayroll.getOctoberTotalIncome())
                .setParameter("novemberTotalIncome", christmasPayroll.getNovemberTotalIncome())
                .setParameter("averageSalary", christmasPayroll.getAverageSalary())
                .setParameter("contributableSalary", christmasPayroll.getContributableSalary())
                .setParameter("liquid", christmasPayroll.getLiquid())
                .setParameter("generatedPayrollId", christmasPayroll.getGeneratedPayroll().getId())
                .setParameter("businessUnitId", christmasPayroll.getBusinessUnit().getId())
                .setParameter("costCenter", "0111")
                .setParameter("area", christmasPayroll.getArea())
                .setParameter("chargeId", christmasPayroll.getCharge().getId())
                .setParameter("jobCategoryId", christmasPayroll.getJobCategory().getId())
                .setParameter("bankAccount", christmasPayroll.getBankAccount())
                .setParameter("bankAccountCurrency", christmasPayroll.getBankAccountCurrency())
                .setParameter("clientCode", christmasPayroll.getClientCode())
                .setParameter("version", 0)
                .setParameter("companyId", 1)
                .executeUpdate();

    }




}
