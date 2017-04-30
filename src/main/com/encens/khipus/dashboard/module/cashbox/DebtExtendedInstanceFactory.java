package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class DebtExtendedInstanceFactory implements InstanceFactory<DebtExtended> {
    public DebtExtended createInstance(DebtExtended cachedObject, Object[] row) {
        if (null == cachedObject) {
            return buildNewInstance(row);
        } else {
            cachedObject.addCareer(buildCareerInstance(row));
            return cachedObject;
        }
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }

    private DebtExtended buildNewInstance(Object[] row) {
        DebtExtended instance = new DebtExtended();
        instance.setFacultyCode((String) row[0]);
        instance.setFacultyName((String) row[1]);

        Career career = buildCareerInstance(row);
        instance.addCareer(career);
        return instance;
    }

    private Career buildCareerInstance(Object[] row) {
        Career instance = new Career();
        instance.setName((String) row[2]);
        instance.setDeposit(buildDebtExtendedData(row, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        instance.setAdmissionRight(buildDebtExtendedData(row, 12, 13, 14, 15, 16, 17, 18, 19, 20));
        instance.setComputer(buildDebtExtendedData(row, 21, 22, 23, 24, 25, 26, 27, 28, 29));
        instance.setHalfYear(buildDebtExtendedData(row, 30, 31, 32, 33, 34, 35, 36, 37, 38));
        instance.setEnrollment(buildDebtExtendedData(row, 39, 40, 41, 42, 43, 44, 45, 46, 47));
        instance.setFirstPay(buildDebtExtendedData(row, 48, 49, 50, 51, 52, 53, 54, 55, 56));
        instance.setSecondPay(buildDebtExtendedData(row, 57, 58, 59, 60, 61, 62, 63, 64, 65));
        instance.setThirdPay(buildDebtExtendedData(row, 66, 67, 68, 69, 70, 71, 72, 73, 74));
        instance.setFourthPay(buildDebtExtendedData(row, 75, 76, 77, 78, 79, 80, 81, 82, 83));
        instance.setFifthPay(buildDebtExtendedData(row, 84, 85, 86, 87, 88, 89, 90, 91, 92));
        instance.setAdditionalTopic(buildDebtExtendedData(row, 93, 94, 95, 96, 97, 98, 99, 100, 101));
        instance.setAdministrationExpense(buildDebtExtendedData(row, 102, 103, 104, 105, 106, 107, 108, 109, 110));
        instance.setFirstAdministrationExpense(buildDebtExtendedData(row, 111, 112, 113, 114, 115, 116, 117, 118, 119));
        instance.setSecondAdministrationExpense(buildDebtExtendedData(row, 120, 121, 122, 123, 124, 125, 126, 127, 128));
        instance.setThirdAdministrationExpense(buildDebtExtendedData(row, 129, 130, 131, 132, 133, 134, 135, 136, 137));
        instance.setFourthAdministrationExpense(buildDebtExtendedData(row, 138, 139, 140, 141, 142, 143, 144, 145, 146));
        instance.setFifthAdministrationExpense(buildDebtExtendedData(row, 147, 148, 149, 150, 151, 152, 153, 154, 155));
        instance.setDelayTopic(buildDebtExtendedData(row, 156, 157, 158, 159, 160, 161, 162, 163, 164));
        instance.setFirstPayDelayTopic(buildDebtExtendedData(row, 165, 166, 167, 168, 169, 170, 171, 172, 173));
        instance.setSecondPayDelayTopic(buildDebtExtendedData(row, 174, 175, 176, 177, 178, 179, 180, 181, 182));
        instance.setThirdPayDelayTopic(buildDebtExtendedData(row, 183, 184, 185, 186, 187, 188, 189, 190, 191));
        instance.setFourthPayDelayTopic(buildDebtExtendedData(row, 192, 193, 194, 195, 196, 197, 198, 199, 200));
        instance.setFifthPayDelayTopic(buildDebtExtendedData(row, 201, 202, 203, 204, 205, 206, 207, 208, 209));
        instance.setHospitalPractice(buildDebtExtendedData(row, 210, 211, 212, 213, 214, 215, 216, 217, 218));
        instance.setTotal(buildDebtExtendedData(row, 219, 220, 221, 222, 223, 224, 225, 226, 227));

        return instance;
    }


    private DebtExtendedAttribute buildDebtExtendedData(Object[] row,
                                                        Integer realCounterIndex,
                                                        Integer paymentCounterIndex,
                                                        Integer debtCounterIndex,
                                                        Integer realBsIndex,
                                                        Integer realUsdIndex,
                                                        Integer paymentBsIndex,
                                                        Integer paymentUsdIndex,
                                                        Integer debtBsIndex,
                                                        Integer debtUsdIndex) {
        DebtExtendedAttribute attribute = new DebtExtendedAttribute();
        attribute.setRealCounter(Integer.valueOf(row[realCounterIndex].toString()));
        attribute.setPaymentCounter(Integer.valueOf(row[paymentCounterIndex].toString()));
        attribute.setDebtCounter(Integer.valueOf(row[debtCounterIndex].toString()));
        attribute.setRealBs((BigDecimal) row[realBsIndex]);
        attribute.setRealUsd((BigDecimal) row[realUsdIndex]);
        attribute.setPaymentBs((BigDecimal) row[paymentBsIndex]);
        attribute.setPaymentUsd((BigDecimal) row[paymentUsdIndex]);
        attribute.setDebtBs((BigDecimal) row[debtBsIndex]);
        attribute.setDebtUsd((BigDecimal) row[debtUsdIndex]);

        return attribute;
    }
}
