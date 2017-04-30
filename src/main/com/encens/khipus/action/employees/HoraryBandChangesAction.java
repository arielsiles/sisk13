package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBand;
import com.encens.khipus.model.employees.HoraryBandChange;
import com.encens.khipus.model.employees.HoraryBandContract;
import com.encens.khipus.service.employees.ContractService;
import com.encens.khipus.service.employees.EmployeeService;
import com.encens.khipus.service.employees.HoraryBandContractService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.EntityManager;
import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


/**
 * Encens Team
 *
 * @author
 * @version : HoraryBandChangesAction, 28-10-2009 03:27:47 PM
 */
@Name("horaryBandChangesAction")
@Scope(ScopeType.CONVERSATION)
public class HoraryBandChangesAction extends GenericAction<HoraryBandChange> {
    private File currentFile = null;

    @In("#{entityManager}")
    private EntityManager em;
    @In
    private EmployeeService employeeService;
    @In
    private ContractService contractService;
    @In
    private HoraryBandContractService horaryBandContractService;


    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }
    /*
    * @param validHoraryBandContract4DateList   list to exclude the inclusive bands.
    * */

    public List<HoraryBandContract> showInclusiveBands(List<HoraryBandContract> validHoraryBandContract4DateList) {
        Collections.sort(validHoraryBandContract4DateList);
        for (int j = 0; j < validHoraryBandContract4DateList.size(); j++) {
            HoraryBandContract hbc1 = validHoraryBandContract4DateList.get(j);
            HoraryBand horaryBand1 = hbc1.getHoraryBand();
            for (HoraryBandContract hbc2 : validHoraryBandContract4DateList) {
                HoraryBand horaryBand2 = hbc2.getHoraryBand();
                if (
                        (((horaryBand1.getEndHour().getTime() > (horaryBand2.getEndHour().getTime())))
                                && ((horaryBand1.getInitHour().getTime() <= (horaryBand2.getInitHour().getTime()))))
                                ||
                                (((horaryBand1.getEndHour().getTime() >= (horaryBand2.getEndHour().getTime())))
                                        && ((horaryBand1.getInitHour().getTime() < (horaryBand2.getInitHour().getTime()))))
                                ||
                                (((horaryBand1.getEndHour().getTime() > (horaryBand2.getEndHour().getTime())))
                                        && ((horaryBand1.getInitHour().getTime() < (horaryBand2.getInitHour().getTime()))))
                                ||
                                (((horaryBand1.getEndHour().getTime() < (horaryBand2.getEndHour().getTime())))
                                        && ((horaryBand1.getInitHour().getTime() > (horaryBand2.getInitHour().getTime()))))
                                ||
                                (((horaryBand1.getEndHour().getTime() > (horaryBand2.getEndHour().getTime())))
                                        && ((horaryBand1.getInitHour().getTime() < (horaryBand2.getInitHour().getTime()))))

                                ||
                                ((((horaryBand1.getEndHour().getTime() > (horaryBand2.getEndHour().getTime())))
                                        && (((horaryBand1.getInitHour().getTime() > (horaryBand2.getInitHour().getTime()))))
                                        && (horaryBand1.getEndHour().getTime() < (horaryBand2.getEndHour().getTime()))))
                                ||
                                ((((horaryBand1.getEndHour().getTime() < (horaryBand2.getEndHour().getTime())))
                                        && (((horaryBand1.getInitHour().getTime() < (horaryBand2.getInitHour().getTime()))))
                                        && (horaryBand2.getInitHour().getTime() < (horaryBand1.getEndHour().getTime()))))


                        ) {
                    log.debug("band:" + hbc1);
                    log.debug("band:" + hbc2);
                }
            }
        }
        return validHoraryBandContract4DateList;
    }

    public void executeAttendanceControl(Calendar firstDay, Calendar lastDay, Calendar controlDayOfMonth, Employee employee,
                                         List<HoraryBandContract> horaryBandContractList4Contract) {
        // iterate all days of the month including the last day
        for (int i = firstDay.get(Calendar.DATE); i <= lastDay.get(Calendar.DATE); i++) {
            // if it isn't sunday
            if (controlDayOfMonth.get(Calendar.DAY_OF_WEEK) != 1) {
                // all special DATES 4 this month
                List<HoraryBandContract> validHoraryBandContract4DateList = horaryBandContractService.findValidHoraryBandContracts4Date(horaryBandContractList4Contract, controlDayOfMonth);
                // map for tolerances of HoraryBand
                Hashtable<Integer, List<HoraryBandContract>> horaryBandContractMapByDay = horaryBandContractService.getHoraryBandContractMapByDay(validHoraryBandContract4DateList);
                // lista de bandas horarias contrato por dia
                List<HoraryBandContract> validDayHoraryBand4DateList = horaryBandContractMapByDay.get(controlDayOfMonth.get(Calendar.DAY_OF_WEEK));
                showInclusiveBands(validDayHoraryBand4DateList);
            } // end if is not sunday

            // go ahead one step in the day of the month for the next iteration
            controlDayOfMonth.add(Calendar.DAY_OF_MONTH, 1);
        }// end for iterate days of month
    }


}
