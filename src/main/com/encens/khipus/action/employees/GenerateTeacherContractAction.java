package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.service.employees.GenerateContractService;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encens S.R.L.
 * Action to generate teacher contract documents in bulk
 *
 * @author
 * @version $Id: GenerateTeacherContractAction.java  24-mar-2010 15:18:43$
 */
@Name("generateTeacherContractAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('TEACHERCONTRACT','VIEW')}")
public class GenerateTeacherContractAction extends GenericAction {

    private String idNumber;
    private BusinessUnit businessUnit;
    private JobCategory jobCategory;
    private Template template;
    private Date initDate;
    private Date endDate;
    private OrganizationalUnit organizationalUnit;
    private Boolean jubilateFlag;

    @In
    private GenerateContractService generateContractService;

    public void generateDocument() {
        log.debug("Generate all teacher contracts.....");
        try {
            template = getService().findById(Template.class, template.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Error in find template.. ", e);
        }
        File file = template.getFile();

        String userSessionId = JSFUtil.getHttpSession(false).getId();
        List<Employee> employeeList = findEmployeesForContractGeneration();

        if (isProfessorGenerationByCareer()) {
            employeeList = generateContractService.filterProfessorsWithMoreGroupsAndAsignatureAssigned(employeeList, organizationalUnit, initDate, endDate);
        }

        try {
            FileInputStream fileInputStream = generateContractService.generateTeacherContractDocumentInTempFile(template, employeeList, businessUnit, initDate, endDate, userSessionId);
            if (fileInputStream.available() > 0) {
                byte[] joinDocument = new byte[fileInputStream.available()];
                fileInputStream.read(joinDocument);
                fileInputStream.close();

                download(joinDocument, file);
            }
        } catch (IOException e) {
            log.debug("Error in generate contract in temp file..", e);
        }
    }

    private List<Employee> findEmployeesForContractGeneration() {
        List<Employee> employeeList = new ArrayList<Employee>();

        //add filters
        EmployeeTeacherContractGenerationDataModel employeeTeacherContractGenerationDataModel = (EmployeeTeacherContractGenerationDataModel) Component.getInstance("employeeTeacherContractGenerationDataModel", ScopeType.EVENT, true, true);
        employeeTeacherContractGenerationDataModel.setIdNumber(getIdNumber());
        employeeTeacherContractGenerationDataModel.setBusinessUnit(getBusinessUnit());
        employeeTeacherContractGenerationDataModel.setOrganizationalUnit(getOrganizationalUnit());
        employeeTeacherContractGenerationDataModel.setJobCategory(getJobCategory());
        employeeTeacherContractGenerationDataModel.setInitDate(getInitDate());
        employeeTeacherContractGenerationDataModel.setEndDate(getEndDate());
        employeeTeacherContractGenerationDataModel.setJubilateFlag(getJubilateFlag());

        employeeList = employeeTeacherContractGenerationDataModel.getResultList();

        return employeeList;
    }

    private boolean isProfessorGenerationByCareer() {
        return organizationalUnit != null && PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobCategory.getPayrollGenerationType());
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public Boolean getJubilateFlag() {
        return jubilateFlag;
    }

    public void setJubilateFlag(Boolean jubilateFlag) {
        this.jubilateFlag = jubilateFlag;
    }

    public void clearOrganizationalUnit() {
        setOrganizationalUnit(null);
    }

    public void assignOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null) {
            try {
                organizationalUnit = getService().findById(OrganizationalUnit.class, organizationalUnit.getId());
            } catch (EntryNotFoundException e) {
                entryNotFoundLog();
            }
            setOrganizationalUnit(organizationalUnit);
            setBusinessUnit(organizationalUnit.getBusinessUnit());
        } else {
            clearOrganizationalUnit();
        }
    }

    public boolean hasOrganizationalUnit() {
        return getOrganizationalUnit() != null;
    }

    /**
     * Download rtf merged document
     *
     * @param mergedDocument contract document
     * @param templateFile   template file
     */
    public void download(byte[] mergedDocument, File templateFile) {

        HttpServletResponse response = JSFUtil.getHttpServletResponse();
        response.setContentType(templateFile.getContentType());
        response.addHeader("Content-disposition", "attachment; filename=\"" + templateFile.getName() + "\"");
        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(mergedDocument);
            os.flush();
            os.close();
            JSFUtil.getFacesContext().responseComplete();
        } catch (Exception e) {
            log.error("\nFailure : " + e.toString() + "\n");
        }
    }

}