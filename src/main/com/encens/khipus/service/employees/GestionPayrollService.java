package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.ExchangeRate;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 * This class implements the GestionPayroll service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface GestionPayrollService {

    GestionPayroll getAllGestionPayroll();

    GestionPayroll findGestionPayrollById(Long id);

    List<GestionPayroll> filterGestionPayroll(Date initDate, Date endDate, Sector sector);

    List<GestionPayroll> findValidGestionPayrolls(Gestion gestion, Month month);

    Long countValidGestionPayrolls(Gestion gestion, Month month);

    List<GestionPayroll> findGestionPayrolls(Gestion gestion, Month month);

    Long countGestionPayrolls(Gestion gestion, Month month);

    Boolean hasOfficialGeneration(GestionPayroll gestionPayroll);

    List<ExchangeRate> findExchangeRateFromGestionPayroll(Gestion gestion, Month month);

    BigDecimal sumLiquidForBankAccount(Gestion gestion, Month month, Long currencyId);

    BigDecimal sumLiquidForCheck(Gestion gestion, Month month);

    BigDecimal sumLiquidForCheck(Gestion gestion, Month month, Long currencyId);

    List<ManagersPayroll> getManagersPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, Month month);

    List<GeneralPayroll> getProfessorsPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, Month month);

    List<ChristmasPayroll> getChristmasPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion);

    GeneratedPayroll findOfficialGeneratedPayroll(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion, GestionPayrollType type, Month month);

    GestionPayroll findAvailableGestionPayroll();

    String getNextGeneratedPayrollName(GestionPayroll gestionPayroll);

    Boolean hasValidGenerationDateRange(GestionPayroll gestionPayroll);

    Boolean hasValidOfficialPayrollDeadline(GestionPayroll gestionPayroll);


    @SuppressWarnings(value = "unchecked")
    List<GestionPayroll> findGestionPayrollByGestionAndBusinessUnitAndMonth(Gestion gestion, Month month,
                                                                            BusinessUnit businessUnit,
                                                                            EntityManager entityManager);

    GestionPayroll findGestionPayrollByGestionAndBusinessUnitAndMonthAndJobCategory(Gestion gestion, Month month,
                                                                                    BusinessUnit businessUnit,
                                                                                    JobCategory jobCategory,
                                                                                    EntityManager entityManager);

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param gestion               a given gestion
     * @param businessUnit          a given businessUnit
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    Long countByGestionAndBusinessUnitAndJobCategoryAndType(Gestion gestion,
                                                            BusinessUnit businessUnit,
                                                            JobCategory jobCategory,
                                                            GestionPayrollType gestionPayrollType,
                                                            boolean useEventEntityManager);

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param businessUnit          a given businessUnit
     * @param gestion               a given gestion
     * @param month                 a given month
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param idList                a list of id to filter for exclude purpose
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    Long countByBusinessUnitAndGestionAndMonthAndJobCategoryAndTypeAndNotInList(
            BusinessUnit businessUnit,
            Gestion gestion,
            Month month,
            JobCategory jobCategory,
            GestionPayrollType gestionPayrollType,
            List<Long> idList,
            boolean useEventEntityManager);

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param businessUnit          a given businessUnit
     * @param gestion               a given gestion
     * @param month                 a given month
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    Long countByBusinessUnitAndGestionAndMonthAndJobCategoryAndType(
            BusinessUnit businessUnit,
            Gestion gestion,
            Month month,
            JobCategory jobCategory,
            GestionPayrollType gestionPayrollType,
            boolean useEventEntityManager);

    /**
     * Counts how many payrolls there are given a set of filters
     *
     * @param gestion               a given gestion
     * @param businessUnit          a given businessUnit
     * @param jobCategory           a given jobCategory
     * @param gestionPayrollType    a given gestionPayrollType
     * @param idList                a list of id to filter for exclude purpose
     * @param useEventEntityManager true if the search will be by event
     * @return how many payrolls there are given a set of filters
     */
    Long countByGestionAndBusinessUnitAndJobCategoryAndTypeNotInIdList(Gestion gestion,
                                                                       BusinessUnit businessUnit,
                                                                       JobCategory jobCategory,
                                                                       GestionPayrollType gestionPayrollType,
                                                                       List<Long> idList,
                                                                       boolean useEventEntityManager);

    List<FiscalProfessorPayroll> getFiscalProfessorPayrollGeneration(BusinessUnit businessUnit, JobCategory jobCategory, Gestion gestion);
}