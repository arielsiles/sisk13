package com.encens.khipus.action.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;
import com.encens.khipus.model.academics.AcademicEmployeePlanning;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.service.employees.AcademicCareerManagerPlanningService;
import com.encens.khipus.service.employees.AcademicEmployeePlanningService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Name("teacherScheduleEvaluationFormAction")
@Scope(ScopeType.CONVERSATION)
public class TeacherScheduleEvaluationFormAction extends GenericScheduleEvaluationFormAction {

    @In
    private AcademicEmployeePlanningService academicEmployeePlanningService;

    @In
    private AcademicCareerManagerPlanningService academicCareerManagerPlanningService;

    @Create
    public void init() {
        initEvaluationForm(PollFormType.TEACHER_POLLFORM);
        
        if (validateRequestParameter()) {

            setPollForm(scheduleEvaluationService.findPollFormByTypeGestionAndPeriod(PollFormType.TEACHER_POLLFORM,
                    gestion,
                    period,
                    new Date()));

            List<AcademicEmployeePlanning> plannings = academicEmployeePlanningService.getPlanning(code, gestion, period);
            if (null == plannings || plannings.isEmpty()) {
                enabledToEvaluate = false;

                return;
            }

            AcademicEmployeePlanning firstPlanning = plannings.get(0);
            Employee employee = academicStructureService.synchronizeEmployee(firstPlanning);
            setEvaluator(employee);

            List<AcademicCareerManagerPlanning> careerPlannings = new ArrayList<AcademicCareerManagerPlanning>();
            for (AcademicEmployeePlanning academicPlanning : plannings) {
                careerPlannings.addAll(academicCareerManagerPlanningService.getAcademicCareerManagerPlanning(
                        academicPlanning.getCareerId(),
                        academicPlanning.getFacultyId(),
                        academicPlanning.getSeatId(),
                        gestion,
                        period));
            }

            setAcademicStructureList(academicStructureService.syncAcademicCareerStructure(careerPlannings));
            if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
                enabledToEvaluate = false;
            }
        }
    }
}
