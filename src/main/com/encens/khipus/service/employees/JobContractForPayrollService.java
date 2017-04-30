package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.JobContract;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface JobContractForPayrollService {

    List<JobContract> getList(int firstRow, int maxResults, String sortProperty, boolean sortAsc,
                              String idNumber, String firstName, String maidenName, String lastName,
                              Date startDate, Date endDate, BusinessUnit businessUnit, PayrollGenerationType payrollGenerationType,
                              List<Long> selectedJobContractIdList, Bonus bonus);

    Long getCount(String sortProperty, boolean sortAsc,
                  String idNumber, String firstName, String maidenName, String lastName,
                  Date startDate, Date endDate, BusinessUnit businessUnit, PayrollGenerationType payrollGenerationType,
                  List<Long> selectedJobContractIdList, Bonus bonus);
}
