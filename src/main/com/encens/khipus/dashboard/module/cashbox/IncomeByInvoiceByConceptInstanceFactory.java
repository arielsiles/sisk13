package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByConceptInstanceFactory implements InstanceFactory<IncomeByInvoiceByConcept> {
    public IncomeByInvoiceByConcept createInstance(IncomeByInvoiceByConcept cachedObject, Object[] row) {
        IncomeByInvoiceByConcept instance = new IncomeByInvoiceByConcept();
        instance.setCode(Integer.valueOf(row[0].toString()));
        instance.setMonth((String) row[3]);
        instance.setUniqueDepositBs((BigDecimal) row[4]);
        instance.setUniqueDepositUsd((BigDecimal) row[5]);
        instance.setAdmissionBs((BigDecimal) row[6]);
        instance.setAdmissionUsd((BigDecimal) row[7]);
        instance.setComputerBs((BigDecimal) row[8]);
        instance.setComputerUsd((BigDecimal) row[9]);
        instance.setHalfYearBs((BigDecimal) row[10]);
        instance.setHalfYearUsd((BigDecimal) row[11]);
        instance.setEnrollmentBs((BigDecimal) row[12]);
        instance.setEnrollmentUsd((BigDecimal) row[13]);
        instance.setPaymentBs((BigDecimal) row[14]);
        instance.setPaymentUsd((BigDecimal) row[15]);
        instance.setExpenseBs((BigDecimal) row[16]);
        instance.setExpenseUsd((BigDecimal) row[17]);
        instance.setAdditionalTopicBs((BigDecimal) row[18]);
        instance.setAdditionalTopicUsd((BigDecimal) row[19]);
        instance.setDelayTopicBs((BigDecimal) row[20]);
        instance.setDelayTopicUsd((BigDecimal) row[21]);
        instance.setCongressBs((BigDecimal) row[22]);
        instance.setCongressUsd((BigDecimal) row[23]);
        instance.setRightChargesBs((BigDecimal) row[24]);
        instance.setRightChargesUsd((BigDecimal) row[25]);
        instance.setDentistryPracticeBs((BigDecimal) row[26]);
        instance.setDentistryPracticeUsd((BigDecimal) row[27]);
        instance.setHospitalPracticeBs((BigDecimal) row[28]);
        instance.setHospitalPracticeUsd((BigDecimal) row[29]);
        instance.setInternshipBs((BigDecimal) row[30]);
        instance.setInternshipUsd((BigDecimal) row[31]);
        instance.setCertificationBs((BigDecimal) row[32]);
        instance.setCertificationUsd((BigDecimal) row[33]);
        instance.setExtracurricularWorkBs((BigDecimal) row[34]);
        instance.setExtracurricularWorkUsd((BigDecimal) row[35]);
        instance.setPaperworkBs((BigDecimal) row[36]);
        instance.setPaperworkUsd((BigDecimal) row[37]);
        instance.setSummerWinterBs((BigDecimal) row[38]);
        instance.setSummerWinterUsd((BigDecimal) row[39]);
        instance.setDuelTopicBs((BigDecimal) row[40]);
        instance.setDuelTopicUsd((BigDecimal) row[41]);
        instance.setExtemporaneousTestBs((BigDecimal) row[42]);
        instance.setExtemporaneousTestUsd((BigDecimal) row[43]);
        instance.setSecondTurnBs((BigDecimal) row[44]);
        instance.setSecondTurnUsd((BigDecimal) row[45]);
        instance.setSouvenirBs((BigDecimal) row[46]);
        instance.setSouvenirUsd((BigDecimal) row[47]);
        instance.setDidacticMaterialBs((BigDecimal) row[48]);
        instance.setDidacticMaterialUsd((BigDecimal) row[49]);
        instance.setAuditoriumRentalBs((BigDecimal) row[50]);
        instance.setAuditoriumRentalUsd((BigDecimal) row[51]);
        instance.setCoffeeShopRentalBs((BigDecimal) row[52]);
        instance.setCoffeeShopRentalUsd((BigDecimal) row[53]);
        instance.setReserveBs((BigDecimal) row[54]);
        instance.setReserveUsd((BigDecimal) row[55]);
        instance.setBs((BigDecimal) row[56]);
        instance.setUsd((BigDecimal) row[57]);
        instance.setMainTotalUsd((BigDecimal) row[58]);
        instance.setExchangeRate((BigDecimal) row[59]);
        instance.setTotalValues();
        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
