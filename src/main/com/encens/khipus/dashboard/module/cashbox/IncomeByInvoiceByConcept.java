package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.totalizer.Sum;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByConcept implements DashboardObject {
    private Integer code;
    private String month;

    @Sum(fieldResultName = "uniqueDepositBs")
    private BigDecimal uniqueDepositBs;
    @Sum(fieldResultName = "uniqueDepositUsd")
    private BigDecimal uniqueDepositUsd;
    @Sum(fieldResultName = "totalUniqueDepositUsd")
    private BigDecimal totalUniqueDepositUsd;

    @Sum(fieldResultName = "admissionBs")
    private BigDecimal admissionBs;
    @Sum(fieldResultName = "admissionUsd")
    private BigDecimal admissionUsd;
    @Sum(fieldResultName = "totalAdmissionUsd")
    private BigDecimal totalAdmissionUsd;

    @Sum(fieldResultName = "computerBs")
    private BigDecimal computerBs;
    @Sum(fieldResultName = "computerUsd")
    private BigDecimal computerUsd;
    @Sum(fieldResultName = "totalComputerUsd")
    private BigDecimal totalComputerUsd;

    @Sum(fieldResultName = "halfYearBs")
    private BigDecimal halfYearBs;
    @Sum(fieldResultName = "halfYearUsd")
    private BigDecimal halfYearUsd;
    @Sum(fieldResultName = "totalHalfYearUsd")
    private BigDecimal totalHalfYearUsd;

    @Sum(fieldResultName = "enrollmentBs")
    private BigDecimal enrollmentBs;
    @Sum(fieldResultName = "enrollmentUsd")
    private BigDecimal enrollmentUsd;
    @Sum(fieldResultName = "totalEnrollmentUsd")
    private BigDecimal totalEnrollmentUsd;

    @Sum(fieldResultName = "paymentBs")
    private BigDecimal paymentBs;
    @Sum(fieldResultName = "paymentUsd")
    private BigDecimal paymentUsd;
    @Sum(fieldResultName = "totalPaymentUsd")
    private BigDecimal totalPaymentUsd;

    @Sum(fieldResultName = "expenseBs")
    private BigDecimal expenseBs;
    @Sum(fieldResultName = "expenseUsd")
    private BigDecimal expenseUsd;
    @Sum(fieldResultName = "totalExpenseUsd")
    private BigDecimal totalExpenseUsd;

    @Sum(fieldResultName = "additionalTopicBs")
    private BigDecimal additionalTopicBs;
    @Sum(fieldResultName = "additionalTopicUsd")
    private BigDecimal additionalTopicUsd;
    @Sum(fieldResultName = "totalAdditionalTopicUsd")
    private BigDecimal totalAdditionalTopicUsd;

    @Sum(fieldResultName = "delayTopicBs")
    private BigDecimal delayTopicBs;
    @Sum(fieldResultName = "delayTopicUsd")
    private BigDecimal delayTopicUsd;
    @Sum(fieldResultName = "totalDelayTopicUsd")
    private BigDecimal totalDelayTopicUsd;

    @Sum(fieldResultName = "congressBs")
    private BigDecimal congressBs;
    @Sum(fieldResultName = "congressUsd")
    private BigDecimal congressUsd;
    @Sum(fieldResultName = "totalCongressUsd")
    private BigDecimal totalCongressUsd;

    @Sum(fieldResultName = "rightChargesBs")
    private BigDecimal rightChargesBs;
    @Sum(fieldResultName = "rightChargesUsd")
    private BigDecimal rightChargesUsd;
    @Sum(fieldResultName = "totalRightChargesUsd")
    private BigDecimal totalRightChargesUsd;

    @Sum(fieldResultName = "dentistryPracticeBs")
    private BigDecimal dentistryPracticeBs;
    @Sum(fieldResultName = "dentistryPracticeUsd")
    private BigDecimal dentistryPracticeUsd;
    @Sum(fieldResultName = "totalDentistryPracticeUsd")
    private BigDecimal totalDentistryPracticeUsd;

    @Sum(fieldResultName = "hospitalPracticeBs")
    private BigDecimal hospitalPracticeBs;
    @Sum(fieldResultName = "hospitalPracticeUsd")
    private BigDecimal hospitalPracticeUsd;
    @Sum(fieldResultName = "totalHospitalPracticeUsd")
    private BigDecimal totalHospitalPracticeUsd;

    @Sum(fieldResultName = "internshipBs")
    private BigDecimal internshipBs;
    @Sum(fieldResultName = "internshipUsd")
    private BigDecimal internshipUsd;
    @Sum(fieldResultName = "totalInternshipUsd")
    private BigDecimal totalInternshipUsd;

    @Sum(fieldResultName = "certificationBs")
    private BigDecimal certificationBs;
    @Sum(fieldResultName = "certificationUsd")
    private BigDecimal certificationUsd;
    @Sum(fieldResultName = "totalCertificationUsd")
    private BigDecimal totalCertificationUsd;

    @Sum(fieldResultName = "extracurricularWorkBs")
    private BigDecimal extracurricularWorkBs;
    @Sum(fieldResultName = "extracurricularWorkUsd")
    private BigDecimal extracurricularWorkUsd;
    @Sum(fieldResultName = "totalExtracurricularWorkUsd")
    private BigDecimal totalExtracurricularWorkUsd;

    @Sum(fieldResultName = "paperworkBs")
    private BigDecimal paperworkBs;
    @Sum(fieldResultName = "paperworkUsd")
    private BigDecimal paperworkUsd;
    @Sum(fieldResultName = "totalPaperworkUsd")
    private BigDecimal totalPaperworkUsd;

    @Sum(fieldResultName = "summerWinterBs")
    private BigDecimal summerWinterBs;
    @Sum(fieldResultName = "summerWinterUsd")
    private BigDecimal summerWinterUsd;
    @Sum(fieldResultName = "totalSummerWinterUsd")
    private BigDecimal totalSummerWinterUsd;

    @Sum(fieldResultName = "duelTopicBs")
    private BigDecimal duelTopicBs;
    @Sum(fieldResultName = "duelTopicUsd")
    private BigDecimal duelTopicUsd;
    @Sum(fieldResultName = "totalDuelTopicUsd")
    private BigDecimal totalDuelTopicUsd;

    @Sum(fieldResultName = "extemporaneousTestBs")
    private BigDecimal extemporaneousTestBs;
    @Sum(fieldResultName = "extemporaneousTestUsd")
    private BigDecimal extemporaneousTestUsd;
    @Sum(fieldResultName = "totalExtemporaneousTestUsd")
    private BigDecimal totalExtemporaneousTestUsd;


    @Sum(fieldResultName = "secondTurnBs")
    private BigDecimal secondTurnBs;
    @Sum(fieldResultName = "secondTurnUsd")
    private BigDecimal secondTurnUsd;
    @Sum(fieldResultName = "totalSecondTurnUsd")
    private BigDecimal totalSecondTurnUsd;

    @Sum(fieldResultName = "souvenirBs")
    private BigDecimal souvenirBs;
    @Sum(fieldResultName = "souvenirUsd")
    private BigDecimal souvenirUsd;
    @Sum(fieldResultName = "totalSouvenirUsd")
    private BigDecimal totalSouvenirUsd;

    @Sum(fieldResultName = "didacticMaterialBs")
    private BigDecimal didacticMaterialBs;
    @Sum(fieldResultName = "didacticMaterialUsd")
    private BigDecimal didacticMaterialUsd;
    @Sum(fieldResultName = "totalDidacticMaterialUsd")
    private BigDecimal totalDidacticMaterialUsd;

    @Sum(fieldResultName = "auditoriumRentalBs")
    private BigDecimal auditoriumRentalBs;
    @Sum(fieldResultName = "auditoriumRentalUsd")
    private BigDecimal auditoriumRentalUsd;
    @Sum(fieldResultName = "totalAuditoriumRentalUsd")
    private BigDecimal totalAuditoriumRentalUsd;

    @Sum(fieldResultName = "coffeeShopRentalBs")
    private BigDecimal coffeeShopRentalBs;
    @Sum(fieldResultName = "coffeeShopRentalUsd")
    private BigDecimal coffeeShopRentalUsd;
    @Sum(fieldResultName = "totalCoffeeShopRentalUsd")
    private BigDecimal totalCoffeeShopRentalUsd;

    @Sum(fieldResultName = "reserveBs")
    private BigDecimal reserveBs;
    @Sum(fieldResultName = "reserveUsd")
    private BigDecimal reserveUsd;
    @Sum(fieldResultName = "totalReserveUsd")
    private BigDecimal totalReserveUsd;

    @Sum(fieldResultName = "bs")
    private BigDecimal bs;
    @Sum(fieldResultName = "usd")
    private BigDecimal usd;
    @Sum(fieldResultName = "totalUsd")
    private BigDecimal totalUsd;

    @Sum(fieldResultName = "mainTotalUsd")
    private BigDecimal mainTotalUsd;

    private BigDecimal exchangeRate;

    public Object getIdentifier() {
        return code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getUniqueDepositBs() {
        return uniqueDepositBs;
    }

    public void setUniqueDepositBs(BigDecimal uniqueDepositBs) {
        this.uniqueDepositBs = uniqueDepositBs;
    }

    public BigDecimal getUniqueDepositUsd() {
        return uniqueDepositUsd;
    }

    public void setUniqueDepositUsd(BigDecimal uniqueDepositUsd) {
        this.uniqueDepositUsd = uniqueDepositUsd;
    }

    public BigDecimal getTotalUniqueDepositUsd() {
        return totalUniqueDepositUsd;
    }

    public void setTotalUniqueDepositUsd(BigDecimal totalUniqueDepositUsd) {
        this.totalUniqueDepositUsd = totalUniqueDepositUsd;
    }

    public BigDecimal getAdmissionBs() {
        return admissionBs;
    }

    public void setAdmissionBs(BigDecimal admissionBs) {
        this.admissionBs = admissionBs;
    }

    public BigDecimal getAdmissionUsd() {
        return admissionUsd;
    }

    public void setAdmissionUsd(BigDecimal admissionUsd) {
        this.admissionUsd = admissionUsd;
    }

    public BigDecimal getTotalAdmissionUsd() {
        return totalAdmissionUsd;
    }

    public void setTotalAdmissionUsd(BigDecimal totalAdmissionUsd) {
        this.totalAdmissionUsd = totalAdmissionUsd;
    }

    public BigDecimal getComputerBs() {
        return computerBs;
    }

    public void setComputerBs(BigDecimal computerBs) {
        this.computerBs = computerBs;
    }

    public BigDecimal getComputerUsd() {
        return computerUsd;
    }

    public void setComputerUsd(BigDecimal computerUsd) {
        this.computerUsd = computerUsd;
    }

    public BigDecimal getTotalComputerUsd() {
        return totalComputerUsd;
    }

    public void setTotalComputerUsd(BigDecimal totalComputerUsd) {
        this.totalComputerUsd = totalComputerUsd;
    }

    public BigDecimal getHalfYearBs() {
        return halfYearBs;
    }

    public void setHalfYearBs(BigDecimal halfYearBs) {
        this.halfYearBs = halfYearBs;
    }

    public BigDecimal getHalfYearUsd() {
        return halfYearUsd;
    }

    public void setHalfYearUsd(BigDecimal halfYearUsd) {
        this.halfYearUsd = halfYearUsd;
    }

    public BigDecimal getTotalHalfYearUsd() {
        return totalHalfYearUsd;
    }

    public void setTotalHalfYearUsd(BigDecimal totalHalfYearUsd) {
        this.totalHalfYearUsd = totalHalfYearUsd;
    }

    public BigDecimal getEnrollmentBs() {
        return enrollmentBs;
    }

    public void setEnrollmentBs(BigDecimal enrollmentBs) {
        this.enrollmentBs = enrollmentBs;
    }

    public BigDecimal getEnrollmentUsd() {
        return enrollmentUsd;
    }

    public void setEnrollmentUsd(BigDecimal enrollmentUsd) {
        this.enrollmentUsd = enrollmentUsd;
    }

    public BigDecimal getTotalEnrollmentUsd() {
        return totalEnrollmentUsd;
    }

    public void setTotalEnrollmentUsd(BigDecimal totalEnrollmentUsd) {
        this.totalEnrollmentUsd = totalEnrollmentUsd;
    }

    public BigDecimal getPaymentBs() {
        return paymentBs;
    }

    public void setPaymentBs(BigDecimal paymentBs) {
        this.paymentBs = paymentBs;
    }

    public BigDecimal getPaymentUsd() {
        return paymentUsd;
    }

    public void setPaymentUsd(BigDecimal paymentUsd) {
        this.paymentUsd = paymentUsd;
    }

    public BigDecimal getTotalPaymentUsd() {
        return totalPaymentUsd;
    }

    public void setTotalPaymentUsd(BigDecimal totalPaymentUsd) {
        this.totalPaymentUsd = totalPaymentUsd;
    }

    public BigDecimal getExpenseBs() {
        return expenseBs;
    }

    public void setExpenseBs(BigDecimal expenseBs) {
        this.expenseBs = expenseBs;
    }

    public BigDecimal getExpenseUsd() {
        return expenseUsd;
    }

    public void setExpenseUsd(BigDecimal expenseUsd) {
        this.expenseUsd = expenseUsd;
    }

    public BigDecimal getTotalExpenseUsd() {
        return totalExpenseUsd;
    }

    public void setTotalExpenseUsd(BigDecimal totalExpenseUsd) {
        this.totalExpenseUsd = totalExpenseUsd;
    }

    public BigDecimal getAdditionalTopicBs() {
        return additionalTopicBs;
    }

    public void setAdditionalTopicBs(BigDecimal additionalTopicBs) {
        this.additionalTopicBs = additionalTopicBs;
    }

    public BigDecimal getAdditionalTopicUsd() {
        return additionalTopicUsd;
    }

    public void setAdditionalTopicUsd(BigDecimal additionalTopicUsd) {
        this.additionalTopicUsd = additionalTopicUsd;
    }

    public BigDecimal getTotalAdditionalTopicUsd() {
        return totalAdditionalTopicUsd;
    }

    public void setTotalAdditionalTopicUsd(BigDecimal totalAdditionalTopicUsd) {
        this.totalAdditionalTopicUsd = totalAdditionalTopicUsd;
    }

    public BigDecimal getDelayTopicBs() {
        return delayTopicBs;
    }

    public void setDelayTopicBs(BigDecimal delayTopicBs) {
        this.delayTopicBs = delayTopicBs;
    }

    public BigDecimal getDelayTopicUsd() {
        return delayTopicUsd;
    }

    public void setDelayTopicUsd(BigDecimal delayTopicUsd) {
        this.delayTopicUsd = delayTopicUsd;
    }

    public BigDecimal getTotalDelayTopicUsd() {
        return totalDelayTopicUsd;
    }

    public void setTotalDelayTopicUsd(BigDecimal totalDelayTopicUsd) {
        this.totalDelayTopicUsd = totalDelayTopicUsd;
    }

    public BigDecimal getCongressBs() {
        return congressBs;
    }

    public void setCongressBs(BigDecimal congressBs) {
        this.congressBs = congressBs;
    }

    public BigDecimal getCongressUsd() {
        return congressUsd;
    }

    public void setCongressUsd(BigDecimal congressUsd) {
        this.congressUsd = congressUsd;
    }

    public BigDecimal getTotalCongressUsd() {
        return totalCongressUsd;
    }

    public void setTotalCongressUsd(BigDecimal totalCongressUsd) {
        this.totalCongressUsd = totalCongressUsd;
    }

    public BigDecimal getRightChargesBs() {
        return rightChargesBs;
    }

    public void setRightChargesBs(BigDecimal rightChargesBs) {
        this.rightChargesBs = rightChargesBs;
    }

    public BigDecimal getRightChargesUsd() {
        return rightChargesUsd;
    }

    public void setRightChargesUsd(BigDecimal rightChargesUsd) {
        this.rightChargesUsd = rightChargesUsd;
    }

    public BigDecimal getTotalRightChargesUsd() {
        return totalRightChargesUsd;
    }

    public void setTotalRightChargesUsd(BigDecimal totalRightChargesUsd) {
        this.totalRightChargesUsd = totalRightChargesUsd;
    }

    public BigDecimal getDentistryPracticeBs() {
        return dentistryPracticeBs;
    }

    public void setDentistryPracticeBs(BigDecimal dentistryPracticeBs) {
        this.dentistryPracticeBs = dentistryPracticeBs;
    }

    public BigDecimal getDentistryPracticeUsd() {
        return dentistryPracticeUsd;
    }

    public void setDentistryPracticeUsd(BigDecimal dentistryPracticeUsd) {
        this.dentistryPracticeUsd = dentistryPracticeUsd;
    }

    public BigDecimal getTotalDentistryPracticeUsd() {
        return totalDentistryPracticeUsd;
    }

    public void setTotalDentistryPracticeUsd(BigDecimal totalDentistryPracticeUsd) {
        this.totalDentistryPracticeUsd = totalDentistryPracticeUsd;
    }

    public BigDecimal getHospitalPracticeBs() {
        return hospitalPracticeBs;
    }

    public void setHospitalPracticeBs(BigDecimal hospitalPracticeBs) {
        this.hospitalPracticeBs = hospitalPracticeBs;
    }

    public BigDecimal getHospitalPracticeUsd() {
        return hospitalPracticeUsd;
    }

    public void setHospitalPracticeUsd(BigDecimal hospitalPracticeUsd) {
        this.hospitalPracticeUsd = hospitalPracticeUsd;
    }

    public BigDecimal getTotalHospitalPracticeUsd() {
        return totalHospitalPracticeUsd;
    }

    public void setTotalHospitalPracticeUsd(BigDecimal totalHospitalPracticeUsd) {
        this.totalHospitalPracticeUsd = totalHospitalPracticeUsd;
    }

    public BigDecimal getInternshipBs() {
        return internshipBs;
    }

    public void setInternshipBs(BigDecimal internshipBs) {
        this.internshipBs = internshipBs;
    }

    public BigDecimal getInternshipUsd() {
        return internshipUsd;
    }

    public void setInternshipUsd(BigDecimal internshipUsd) {
        this.internshipUsd = internshipUsd;
    }

    public BigDecimal getTotalInternshipUsd() {
        return totalInternshipUsd;
    }

    public void setTotalInternshipUsd(BigDecimal totalInternshipUsd) {
        this.totalInternshipUsd = totalInternshipUsd;
    }

    public BigDecimal getCertificationBs() {
        return certificationBs;
    }

    public void setCertificationBs(BigDecimal certificationBs) {
        this.certificationBs = certificationBs;
    }

    public BigDecimal getCertificationUsd() {
        return certificationUsd;
    }

    public void setCertificationUsd(BigDecimal certificationUsd) {
        this.certificationUsd = certificationUsd;
    }

    public BigDecimal getTotalCertificationUsd() {
        return totalCertificationUsd;
    }

    public void setTotalCertificationUsd(BigDecimal totalCertificationUsd) {
        this.totalCertificationUsd = totalCertificationUsd;
    }

    public BigDecimal getExtracurricularWorkBs() {
        return extracurricularWorkBs;
    }

    public void setExtracurricularWorkBs(BigDecimal extracurricularWorkBs) {
        this.extracurricularWorkBs = extracurricularWorkBs;
    }

    public BigDecimal getExtracurricularWorkUsd() {
        return extracurricularWorkUsd;
    }

    public void setExtracurricularWorkUsd(BigDecimal extracurricularWorkUsd) {
        this.extracurricularWorkUsd = extracurricularWorkUsd;
    }

    public BigDecimal getTotalExtracurricularWorkUsd() {
        return totalExtracurricularWorkUsd;
    }

    public void setTotalExtracurricularWorkUsd(BigDecimal totalExtracurricularWorkUsd) {
        this.totalExtracurricularWorkUsd = totalExtracurricularWorkUsd;
    }

    public BigDecimal getPaperworkBs() {
        return paperworkBs;
    }

    public void setPaperworkBs(BigDecimal paperworkBs) {
        this.paperworkBs = paperworkBs;
    }

    public BigDecimal getPaperworkUsd() {
        return paperworkUsd;
    }

    public void setPaperworkUsd(BigDecimal paperworkUsd) {
        this.paperworkUsd = paperworkUsd;
    }

    public BigDecimal getTotalPaperworkUsd() {
        return totalPaperworkUsd;
    }

    public void setTotalPaperworkUsd(BigDecimal totalPaperworkUsd) {
        this.totalPaperworkUsd = totalPaperworkUsd;
    }

    public BigDecimal getSummerWinterBs() {
        return summerWinterBs;
    }

    public void setSummerWinterBs(BigDecimal summerWinterBs) {
        this.summerWinterBs = summerWinterBs;
    }

    public BigDecimal getSummerWinterUsd() {
        return summerWinterUsd;
    }

    public void setSummerWinterUsd(BigDecimal summerWinterUsd) {
        this.summerWinterUsd = summerWinterUsd;
    }

    public BigDecimal getTotalSummerWinterUsd() {
        return totalSummerWinterUsd;
    }

    public void setTotalSummerWinterUsd(BigDecimal totalSummerWinterUsd) {
        this.totalSummerWinterUsd = totalSummerWinterUsd;
    }

    public BigDecimal getDuelTopicBs() {
        return duelTopicBs;
    }

    public void setDuelTopicBs(BigDecimal duelTopicBs) {
        this.duelTopicBs = duelTopicBs;
    }

    public BigDecimal getDuelTopicUsd() {
        return duelTopicUsd;
    }

    public void setDuelTopicUsd(BigDecimal duelTopicUsd) {
        this.duelTopicUsd = duelTopicUsd;
    }

    public BigDecimal getTotalDuelTopicUsd() {
        return totalDuelTopicUsd;
    }

    public void setTotalDuelTopicUsd(BigDecimal totalDuelTopicUsd) {
        this.totalDuelTopicUsd = totalDuelTopicUsd;
    }

    public BigDecimal getExtemporaneousTestBs() {
        return extemporaneousTestBs;
    }

    public void setExtemporaneousTestBs(BigDecimal extemporaneousTestBs) {
        this.extemporaneousTestBs = extemporaneousTestBs;
    }

    public BigDecimal getExtemporaneousTestUsd() {
        return extemporaneousTestUsd;
    }

    public void setExtemporaneousTestUsd(BigDecimal extemporaneousTestUsd) {
        this.extemporaneousTestUsd = extemporaneousTestUsd;
    }

    public BigDecimal getTotalExtemporaneousTestUsd() {
        return totalExtemporaneousTestUsd;
    }

    public void setTotalExtemporaneousTestUsd(BigDecimal totalExtemporaneousTestUsd) {
        this.totalExtemporaneousTestUsd = totalExtemporaneousTestUsd;
    }

    public BigDecimal getSecondTurnBs() {
        return secondTurnBs;
    }

    public void setSecondTurnBs(BigDecimal secondTurnBs) {
        this.secondTurnBs = secondTurnBs;
    }

    public BigDecimal getSecondTurnUsd() {
        return secondTurnUsd;
    }

    public void setSecondTurnUsd(BigDecimal secondTurnUsd) {
        this.secondTurnUsd = secondTurnUsd;
    }

    public BigDecimal getTotalSecondTurnUsd() {
        return totalSecondTurnUsd;
    }

    public void setTotalSecondTurnUsd(BigDecimal totalSecondTurnUsd) {
        this.totalSecondTurnUsd = totalSecondTurnUsd;
    }

    public BigDecimal getSouvenirBs() {
        return souvenirBs;
    }

    public void setSouvenirBs(BigDecimal souvenirBs) {
        this.souvenirBs = souvenirBs;
    }

    public BigDecimal getSouvenirUsd() {
        return souvenirUsd;
    }

    public void setSouvenirUsd(BigDecimal souvenirUsd) {
        this.souvenirUsd = souvenirUsd;
    }

    public BigDecimal getTotalSouvenirUsd() {
        return totalSouvenirUsd;
    }

    public void setTotalSouvenirUsd(BigDecimal totalSouvenirUsd) {
        this.totalSouvenirUsd = totalSouvenirUsd;
    }

    public BigDecimal getDidacticMaterialBs() {
        return didacticMaterialBs;
    }

    public void setDidacticMaterialBs(BigDecimal didacticMaterialBs) {
        this.didacticMaterialBs = didacticMaterialBs;
    }

    public BigDecimal getDidacticMaterialUsd() {
        return didacticMaterialUsd;
    }

    public void setDidacticMaterialUsd(BigDecimal didacticMaterialUsd) {
        this.didacticMaterialUsd = didacticMaterialUsd;
    }

    public BigDecimal getTotalDidacticMaterialUsd() {
        return totalDidacticMaterialUsd;
    }

    public void setTotalDidacticMaterialUsd(BigDecimal totalDidacticMaterialUsd) {
        this.totalDidacticMaterialUsd = totalDidacticMaterialUsd;
    }

    public BigDecimal getAuditoriumRentalBs() {
        return auditoriumRentalBs;
    }

    public void setAuditoriumRentalBs(BigDecimal auditoriumRentalBs) {
        this.auditoriumRentalBs = auditoriumRentalBs;
    }

    public BigDecimal getAuditoriumRentalUsd() {
        return auditoriumRentalUsd;
    }

    public void setAuditoriumRentalUsd(BigDecimal auditoriumRentalUsd) {
        this.auditoriumRentalUsd = auditoriumRentalUsd;
    }

    public BigDecimal getTotalAuditoriumRentalUsd() {
        return totalAuditoriumRentalUsd;
    }

    public void setTotalAuditoriumRentalUsd(BigDecimal totalAuditoriumRentalUsd) {
        this.totalAuditoriumRentalUsd = totalAuditoriumRentalUsd;
    }

    public BigDecimal getCoffeeShopRentalBs() {
        return coffeeShopRentalBs;
    }

    public void setCoffeeShopRentalBs(BigDecimal coffeeShopRentalBs) {
        this.coffeeShopRentalBs = coffeeShopRentalBs;
    }

    public BigDecimal getCoffeeShopRentalUsd() {
        return coffeeShopRentalUsd;
    }

    public void setCoffeeShopRentalUsd(BigDecimal coffeeShopRentalUsd) {
        this.coffeeShopRentalUsd = coffeeShopRentalUsd;
    }

    public BigDecimal getTotalCoffeeShopRentalUsd() {
        return totalCoffeeShopRentalUsd;
    }

    public void setTotalCoffeeShopRentalUsd(BigDecimal totalCoffeeShopRentalUsd) {
        this.totalCoffeeShopRentalUsd = totalCoffeeShopRentalUsd;
    }

    public BigDecimal getReserveBs() {
        return reserveBs;
    }

    public void setReserveBs(BigDecimal reserveBs) {
        this.reserveBs = reserveBs;
    }

    public BigDecimal getReserveUsd() {
        return reserveUsd;
    }

    public void setReserveUsd(BigDecimal reserveUsd) {
        this.reserveUsd = reserveUsd;
    }

    public BigDecimal getTotalReserveUsd() {
        return totalReserveUsd;
    }

    public void setTotalReserveUsd(BigDecimal totalReserveUsd) {
        this.totalReserveUsd = totalReserveUsd;
    }

    public BigDecimal getBs() {
        return bs;
    }

    public void setBs(BigDecimal bs) {
        this.bs = bs;
    }

    public BigDecimal getUsd() {
        return usd;
    }

    public void setUsd(BigDecimal usd) {
        this.usd = usd;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(BigDecimal totalUsd) {
        this.totalUsd = totalUsd;
    }

    public BigDecimal getMainTotalUsd() {
        return mainTotalUsd;
    }

    public void setMainTotalUsd(BigDecimal mainTotalUsd) {
        this.mainTotalUsd = mainTotalUsd;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setTotalValues() {
        totalUniqueDepositUsd = BigDecimalUtil.sum(uniqueDepositUsd, BigDecimalUtil.divide(uniqueDepositBs, exchangeRate));
        totalAdmissionUsd = BigDecimalUtil.sum(admissionUsd, BigDecimalUtil.divide(admissionBs, exchangeRate));
        totalComputerUsd = BigDecimalUtil.sum(computerUsd, BigDecimalUtil.divide(computerBs, exchangeRate));
        totalHalfYearUsd = BigDecimalUtil.sum(halfYearUsd, BigDecimalUtil.divide(halfYearBs, exchangeRate));
        totalEnrollmentUsd = BigDecimalUtil.sum(enrollmentUsd, BigDecimalUtil.divide(enrollmentBs, exchangeRate));
        totalPaymentUsd = BigDecimalUtil.sum(paymentUsd, BigDecimalUtil.divide(paymentBs, exchangeRate));
        totalExpenseUsd = BigDecimalUtil.sum(expenseUsd, BigDecimalUtil.divide(expenseBs, exchangeRate));
        totalAdditionalTopicUsd = BigDecimalUtil.sum(additionalTopicUsd, BigDecimalUtil.divide(additionalTopicBs, exchangeRate));
        totalDelayTopicUsd = BigDecimalUtil.sum(delayTopicUsd, BigDecimalUtil.divide(delayTopicBs, exchangeRate));
        totalCongressUsd = BigDecimalUtil.sum(congressUsd, BigDecimalUtil.divide(congressBs, exchangeRate));
        totalRightChargesUsd = BigDecimalUtil.sum(rightChargesUsd, BigDecimalUtil.divide(rightChargesBs, exchangeRate));
        totalDentistryPracticeUsd = BigDecimalUtil.sum(dentistryPracticeUsd, BigDecimalUtil.divide(dentistryPracticeBs, exchangeRate));
        totalHospitalPracticeUsd = BigDecimalUtil.sum(hospitalPracticeUsd, BigDecimalUtil.divide(hospitalPracticeBs, exchangeRate));
        totalInternshipUsd = BigDecimalUtil.sum(internshipUsd, BigDecimalUtil.divide(internshipBs, exchangeRate));
        totalCertificationUsd = BigDecimalUtil.sum(certificationUsd, BigDecimalUtil.divide(certificationBs, exchangeRate));
        totalExtracurricularWorkUsd = BigDecimalUtil.sum(extracurricularWorkUsd, BigDecimalUtil.divide(extracurricularWorkBs, exchangeRate));
        totalPaperworkUsd = BigDecimalUtil.sum(paperworkUsd, BigDecimalUtil.divide(paperworkBs, exchangeRate));
        totalSummerWinterUsd = BigDecimalUtil.sum(summerWinterUsd, BigDecimalUtil.divide(summerWinterBs, exchangeRate));
        totalDuelTopicUsd = BigDecimalUtil.sum(duelTopicUsd, BigDecimalUtil.divide(duelTopicBs, exchangeRate));
        totalExtemporaneousTestUsd = BigDecimalUtil.sum(extemporaneousTestUsd, BigDecimalUtil.divide(extemporaneousTestBs, exchangeRate));
        totalSecondTurnUsd = BigDecimalUtil.sum(secondTurnUsd, BigDecimalUtil.divide(secondTurnBs, exchangeRate));
        totalSouvenirUsd = BigDecimalUtil.sum(souvenirUsd, BigDecimalUtil.divide(souvenirBs, exchangeRate));
        totalDidacticMaterialUsd = BigDecimalUtil.sum(didacticMaterialUsd, BigDecimalUtil.divide(didacticMaterialBs, exchangeRate));
        totalAuditoriumRentalUsd = BigDecimalUtil.sum(auditoriumRentalUsd, BigDecimalUtil.divide(auditoriumRentalBs, exchangeRate));
        totalCoffeeShopRentalUsd = BigDecimalUtil.sum(coffeeShopRentalUsd, BigDecimalUtil.divide(coffeeShopRentalBs, exchangeRate));
        totalReserveUsd = BigDecimalUtil.sum(reserveUsd, BigDecimalUtil.divide(reserveBs, exchangeRate));
        totalUsd = BigDecimalUtil.sum(usd, BigDecimalUtil.divide(bs, exchangeRate));
    }
}

