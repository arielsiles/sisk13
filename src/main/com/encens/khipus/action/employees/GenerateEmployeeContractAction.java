package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.template.DocumentValues;
import com.encens.khipus.util.template.RTFTemplateUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate employee contracts rtf documents.
 *
 * @author
 * @version $Id: GenerateEmployeeContractAction.java  17-mar-2010 18:45:46$
 */
@Name("generateEmployeeContractAction")
@Scope(ScopeType.CONVERSATION)
public class GenerateEmployeeContractAction extends GenericAction {

    private Employee employee;
    private JobContract jobContract;
    private Template template;

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String generateForwad(Employee employee, JobContract jobContract) {
        setEmployee(employee);
        setJobContract(jobContract);

        return Outcome.SUCCESS;
    }

    public void generateDocument() {
        log.debug("GENERATE DOCUMENT......" + template + "-" + employee + "-" + jobContract);

        try {
            template = getService().findById(Template.class, template.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Error in find template.. ", e);
        }

        File file = template.getFile();

        DocumentValues documentValues = new DocumentValues();
        Map variableValuesMap = documentValues.getEmployeeContractValues(employee.getId(), jobContract.getId());

        RTFTemplateUtil rtfTemplateUtil = new RTFTemplateUtil(file.getValue());
        rtfTemplateUtil.addVariableValues(variableValuesMap);
        byte[] mergedDocument = null;
        try {
            mergedDocument = rtfTemplateUtil.mergeTemplate();
        } catch (Exception e) {
            log.warn("ERROR IN GENERATE RTF DOCUMENT.....", e);
        }

        if (mergedDocument != null) {
            download(mergedDocument, file);
        }
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Download rtf merged document
     * @param mergedDocument contract document
     * @param templateFile template file
     */
    public void download(byte[] mergedDocument,File templateFile) {

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