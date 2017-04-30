package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.KhipusCacheManager;
import com.encens.khipus.util.template.DocumentValues;
import com.encens.khipus.util.template.RTFTemplateUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Service to generate contracts documents
 *
 * @author
 * @version $Id: GenerateContractServiceBean.java  18-may-2010 14:50:42$
 */
@Name("generateContractService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class GenerateContractServiceBean implements GenerateContractService {

    @Logger
    private Log log;

    @In
    private ContractService contractService;

    /**
     * Genarate teacher contracts, save this documents in temporal cache and merge this in one only document
     *
     * @param template
     * @param businessUnit
     * @param initDate
     * @param endDate
     * @param sessionId
     * @return FileInputStream
     * @throws IOException
     */
    public FileInputStream generateTeacherContractDocumentInTempFile(Template template, List<Employee> employeeList, BusinessUnit businessUnit, Date initDate, Date endDate, String sessionId) throws IOException {
        log.debug("Execute generateTeacherContractDocumentInTempFile......");
        String contractDocumentName = "contract";
        String mergedDocumentName = "allMergedContracts";

        File file = template.getFile();

        log.debug("Generate contracts to employees:" + employeeList.size());

        //delete all before temporal contracts for this session id
        KhipusCacheManager.deleteContractDocumentFolder(sessionId);

        String tempContractFolderPath = KhipusCacheManager.pathTempContractFolderCreateIfNotExist(sessionId, true);

        for (int i = 0; i < employeeList.size(); i++) {
            Employee employee = employeeList.get(i);

            DocumentValues documentValues = new DocumentValues();
            Map variableValuesMap = documentValues.getTeacherVariableValues(businessUnit, employee, initDate, endDate);

            RTFTemplateUtil rtfTemplateUtil = new RTFTemplateUtil(file.getValue());
            rtfTemplateUtil.addVariableValues(variableValuesMap);

            String rtfDocumentPath = tempContractFolderPath + String.valueOf(i) + contractDocumentName + employee.getId() + ".rtf";
            java.io.File rtfDocumentFile = new java.io.File(rtfDocumentPath);

            try {
                rtfTemplateUtil.mergeTemplate(rtfDocumentFile);
            } catch (Exception e) {
                log.warn("ERROR IN GENERATE RTF DOCUMENT.....", e);
            }
        }

        //join generated documents
        java.io.File tempContractDirFile = new java.io.File(tempContractFolderPath);
        String allRtfContractsFilePath = KhipusCacheManager.pathContractDocumentFolderCreateIfNotExist(sessionId, true) + mergedDocumentName + ".rtf";

        FileWriter fileWriter = new FileWriter(allRtfContractsFilePath);
        RTFTemplateUtil.joinAllRtfDocumentsInDirentory(tempContractDirFile, fileWriter);
        fileWriter.close();

        //read the merged all contract documents
        FileInputStream fileInputStream = new FileInputStream(allRtfContractsFilePath);

        return fileInputStream;
    }


    /**
     * Generate accidental association document for employees
     *
     * @param template
     * @param businessUnit
     * @param organizationalUnit
     * @return ByteArrayInputStream
     * @throws Exception
     */
    public ByteArrayInputStream generateAccidentalAssociationDocument(Template template, BusinessUnit businessUnit, OrganizationalUnit organizationalUnit, Date contractInitDate, Date contractEndDate) throws Exception {
        log.debug("Execute generateAccidentalAssociationDocument......" + template + "-" + businessUnit + "-" + organizationalUnit);

        DocumentValues documentValues = new DocumentValues();
        Map variableValuesMap = documentValues.getAccidentalAssociationEmployeeListVariableValues(businessUnit, organizationalUnit, contractInitDate, contractEndDate);

        File templateFile = template.getFile();
        RTFTemplateUtil rtfTemplateUtil = new RTFTemplateUtil(templateFile.getValue());
        rtfTemplateUtil.addVariableValues(variableValuesMap);
        byte[] mergedDocument = rtfTemplateUtil.mergeTemplate();

        return new ByteArrayInputStream(mergedDocument);
    }

    /**
     * Filter professor with more groups and asignatures in this organizational unit (career)
     * @param employeeList list
     * @param organizationalUnitFilter filter
     * @param initDate contract init date
     * @param endDate contract end date
     * @return List
     */
    public List<Employee> filterProfessorsWithMoreGroupsAndAsignatureAssigned(List<Employee> employeeList, OrganizationalUnit organizationalUnitFilter, Date initDate, Date endDate) {
        List<Employee> validEmployeeList = new ArrayList<Employee>();

        for (Employee employee : employeeList) {
            OrganizationalUnit maxOrganizationalUnit = contractService.getMaxOrganizationalUnitProfessorAssigned(employee, initDate, endDate);
            if (maxOrganizationalUnit != null && maxOrganizationalUnit.getId().equals(organizationalUnitFilter.getId())) {
                validEmployeeList.add(employee);
            }
        }

        return validEmployeeList;
    }

}
