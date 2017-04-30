package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.contacts.Template;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.OrganizationalUnit;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: GenerateContractService.java  18-may-2010 14:51:52$
 */
public interface GenerateContractService {

    FileInputStream generateTeacherContractDocumentInTempFile(Template template, List<Employee> employeeList, BusinessUnit businessUnit, Date initDate, Date endDate, String sessionId) throws IOException;

    ByteArrayInputStream generateAccidentalAssociationDocument(Template template, BusinessUnit businessUnit, OrganizationalUnit organizationalUnit, Date contractInitDate, Date contractEndDate) throws Exception;

    List<Employee> filterProfessorsWithMoreGroupsAndAsignatureAssigned(List<Employee> employeeList, OrganizationalUnit organizationalUnitFilter, Date initDate, Date endDate);
}
