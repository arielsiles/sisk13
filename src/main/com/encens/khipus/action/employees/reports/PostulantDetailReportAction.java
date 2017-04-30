package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.employees.ExperienceType;
import com.encens.khipus.model.employees.Postulant;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate postulant detail report
 *
 * @author
 * @version $Id: PostulantDetailReportAction.java  13-ene-2010 12:14:59$
 */
@Name("postulantDetailReportAction")
@Scope(ScopeType.PAGE)
public class PostulantDetailReportAction extends GenericReportAction {

    private ExperienceType experienceType;
    private Postulant postulant;

    public void generateReport(Postulant postulant) {
        //add postulant filter used with seam
        setPostulant(postulant);

        Map params = new HashMap();
        setReportFormat(ReportFormat.PDF);

        //add academic formation sub report
        TypedReportData academicFormationSubReportData = generateAcademicFormationSubReport();
        params.putAll(academicFormationSubReportData.getReportParams());
        params.put("ACADEMICFORMATION_SUBREPORT", academicFormationSubReportData.getJasperReport());

        //add laboral experience sub report
        addLaboralExperienceSubReport(params);
        //add professor experience sub report
        addProfessorExperienceSubReport(params);
        //add subject postulate sub report
        addSubjectPostulateSubReport(params);
        //add charge postulate sub report
        addChargePostulateSubReport(params);
        //add hour available sub report
        addHourAvailableSubReport(params);

        super.generateReport("postulantDetailReport", "/employees/reports/postulantDetailReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.postulantDetailReport.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "postulant.id," +
                "postulant.lastName," +
                "postulant.maidenName," +
                "postulant.firstName," +
                "postulant.email," +
                "postulant.cellPhoneNumber," +
                "postulant.phoneNumber," +
                "postulant.birthDay," +
                "postulant.birthPlace," +
                "postulant.idNumber," +
                "postulant.expendedPlace," +
                "postulant.gender," +
                "nationalPrise.value," +
                "internationalPrise.value," +
                "books.value," +
                "nationalArticles.value," +
                "internationalArticles.value," +
                "postulant.postulantType " +
                " FROM Postulant postulant" +
                " left join postulant.nationalPrise nationalPrise" +
                " left join postulant.internationalPrise internationalPrise" +
                " left join postulant.books books" +
                " left join postulant.nationalArticles nationalArticles" +
                " left join postulant.internationalArticles internationalArticles";
    }

    @Create
    public void init() {
        restrictions = new String[]{"postulant=#{postulantDetailReportAction.postulant}"};
        sortProperty = "postulant.registryDate";
    }

    /**
     * generate academic formation sub report
     *
     * @return TypedReportData
     */
    private TypedReportData generateAcademicFormationSubReport() {
        log.debug("Generating generateAcademicFormationSubReport.................................... ");
        Map<String, Object> params = new HashMap<String, Object>();
        String ejbql = "SELECT " +
                "formation.id," +
                "formation.name," +
                "formation.university," +
                "formation.academicFormationType" +
                " FROM AcademicFormation formation" +
                " WHERE formation.postulant.id=$P{postulantIdParam} ";

        String[] restrictions = new String[]{};
        String pollByCareerOrder = "formation.name";

        return (super.generateSubReport(
                "ACADEMICFORMATION_SUBREPORT",
                "/employees/reports/academicFormationSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("ACADEMICFORMATION_SUBREPORT", ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params));
    }

    /**
     * generate experience sub report, params define the type of experience as filter
     *
     * @param reportKeyId    sub report key
     * @param experienceType type
     * @param messageParams  params of sub report
     * @return TypedReportData
     */
    private TypedReportData generateExperienceSubReport(String reportKeyId, ExperienceType experienceType, Map<String, Object> messageParams) {
        log.debug("Generating generateExperienceSubReport.................................... ");
        //set the experience type, this is mapped as restriction with seam
        setExperienceType(experienceType);

        Map<String, Object> params = new HashMap<String, Object>(messageParams);
        String ejbql = "SELECT " +
                "experience.id," +
                "experience.place," +
                "experience.position," +
                "experience.initDate," +
                "experience.endDate" +
                " FROM Experience experience" +
                " WHERE experience.postulant.id=$P{postulantIdParam}";

        String[] restrictions = new String[]{"experience.experienceType=#{postulantDetailReportAction.experienceType}"};
        String pollByCareerOrder = "experience.place";

        return (super.generateSubReport(
                reportKeyId,
                "/employees/reports/experienceSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(reportKeyId, ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params));
    }

    /**
     * Add the sub report in main report
     *
     * @param mainReportParams main report params
     */
    private void addLaboralExperienceSubReport(Map mainReportParams) {
        String subReportKey = "LABORALEXPERIENCESUBREPORT";
        Map<String, Object> messagesParamMap = new HashMap<String, Object>();

        TypedReportData laboralExperienceSubReportData = generateExperienceSubReport(subReportKey, ExperienceType.LABORAL, messagesParamMap);
        mainReportParams.putAll(laboralExperienceSubReportData.getReportParams());
        mainReportParams.put(subReportKey, laboralExperienceSubReportData.getJasperReport());
    }

    /**
     * Add the sub report in main report
     *
     * @param mainReportParams main report params
     */
    private void addProfessorExperienceSubReport(Map mainReportParams) {
        String subReportKey = "PROFESSOREXPERIENCESUBREPORT";
        Map<String, Object> messagesParamMap = new HashMap<String, Object>();

        TypedReportData professorExperienceSubReportData = generateExperienceSubReport(subReportKey, ExperienceType.PROFESSOR, messagesParamMap);
        mainReportParams.putAll(professorExperienceSubReportData.getReportParams());
        mainReportParams.put(subReportKey, professorExperienceSubReportData.getJasperReport());
    }

    /**
     * generate subject postulate sub report
     *
     * @param reportKeyId sub report key
     * @return TypedReportData
     */
    private TypedReportData generateSubjectPostulateSubReport(String reportKeyId) {
        log.debug("Generating generateSubjectPostulateSubReport.................................... ");
        Map<String, Object> params = new HashMap<String, Object>();
        String ejbql = "SELECT " +
                "postulant.id," +
                "subjec.career.faculty.location.name," +
                "subjec.career.faculty.name," +
                "subjec.career.name," +
                "subjec.name" +
                " FROM Postulant postulant" +
                " LEFT JOIN postulant.subjectList subjec" +
                " WHERE postulant.id=$P{postulantIdParam}";

        String[] restrictions = new String[]{};
        String pollByCareerOrder = "subjec.career.faculty.location.name";

        return (super.generateSubReport(
                reportKeyId,
                "/employees/reports/subjectPostulateSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(reportKeyId, ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params));
    }

    /**
     * Add the sub report in main report
     *
     * @param mainReportParams main report params
     */
    private void addSubjectPostulateSubReport(Map mainReportParams) {
        String subReportKey = "SUBJECTPOSTULATESUBREPORT";

        TypedReportData subjectPostulateSubReportData = generateSubjectPostulateSubReport(subReportKey);
        mainReportParams.putAll(subjectPostulateSubReportData.getReportParams());
        mainReportParams.put(subReportKey, subjectPostulateSubReportData.getJasperReport());
    }

    /**
     * generate subject postulate sub report
     *
     * @param reportKeyId sub report key
     * @return TypedReportData
     */
    private TypedReportData generateChargePostulateSubReport(String reportKeyId) {
        log.debug("Generating generateChargePostulateSubReport.................................... ");
        Map<String, Object> params = new HashMap<String, Object>();
        String ejbql = "SELECT " +
                " postulant.id," +
                " postulantCharge.businessUnit.publicity," +
                " postulantCharge.charge.name" +
                " FROM Postulant postulant" +
                " LEFT JOIN postulant.postulantChargeList postulantCharge" +
                " WHERE postulant.id=$P{postulantIdParam}";

        String[] restrictions = new String[]{};
        String pollByChargeOrder = "postulantCharge.charge.name";

        return (super.generateSubReport(
                reportKeyId,
                "/employees/reports/chargePostulateSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(reportKeyId, ejbql, Arrays.asList(restrictions), pollByChargeOrder),
                params));
    }

    /**
     * Add the sub report in main report
     *
     * @param mainReportParams main report params
     */
    private void addChargePostulateSubReport(Map mainReportParams) {
        String subReportKey = "CHARGEPOSTULATESUBREPORT";

        TypedReportData chargePostulateSubReportData = generateChargePostulateSubReport(subReportKey);
        mainReportParams.putAll(chargePostulateSubReportData.getReportParams());
        mainReportParams.put(subReportKey, chargePostulateSubReportData.getJasperReport());
    }

    /**
     * generate the available hour sub report
     *
     * @param reportKeyId sub report key
     * @return TypedReportData
     */
    private TypedReportData generateHourAvailableSubReport(String reportKeyId) {
        log.debug("Generating generateHourAvailableSubReport.................................... ");
        Map<String, Object> params = new HashMap<String, Object>();
        String ejbql = "SELECT " +
                "hourAvailable.id," +
                "hourAvailable.initHour," +
                "hourAvailable.endHour," +
                "hourAvailable.availableDay" +
                " FROM HourAvailable hourAvailable" +
                " WHERE hourAvailable.postulant.id=$P{postulantIdParam}";

        String[] restrictions = new String[]{};
        String pollByCareerOrder = "hourAvailable.initHour";

        return (super.generateSubReport(
                reportKeyId,
                "/employees/reports/hourAvailableSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(reportKeyId, ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params));
    }

    /**
     * Add the sub report in main report
     *
     * @param mainReportParams main report params
     */
    private void addHourAvailableSubReport(Map mainReportParams) {
        String subReportKey = "HOURAVAILABLESUBREPORT";

        TypedReportData hourAvailableSubReportData = generateHourAvailableSubReport(subReportKey);
        mainReportParams.putAll(hourAvailableSubReportData.getReportParams());
        mainReportParams.put(subReportKey, hourAvailableSubReportData.getJasperReport());
    }

    public ExperienceType getExperienceType() {
        return experienceType;
    }

    public void setExperienceType(ExperienceType experienceType) {
        this.experienceType = experienceType;
    }

    public Postulant getPostulant() {
        return postulant;
    }

    public void setPostulant(Postulant postulant) {
        this.postulant = postulant;
    }
}
