package com.encens.khipus.action.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;
import com.encens.khipus.model.academics.AcademicEmployeePlanning;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.service.employees.AcademicCareerManagerPlanningService;
import com.encens.khipus.service.employees.AcademicEmployeePlanningService;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.academic.AcademicStructure;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.List;

/**
 * CareerManagerScheduleEvaluationFormAction
 *
 * @author
 * @version 2.24
 */
@Name("careerManagerScheduleEvaluationFormAction")
@Scope(ScopeType.CONVERSATION)
public class CareerManagerScheduleEvaluationFormAction extends GenericScheduleEvaluationFormAction {
    @In
    private AcademicEmployeePlanningService academicEmployeePlanningService;

    @In
    private AcademicCareerManagerPlanningService academicCareerManagerPlanningService;

    @Create
    public void init() {
        initEvaluationForm(PollFormType.CAREERMANAGER_POLLFORM);

        if (validateRequestParameter()) {
            setPollForm(scheduleEvaluationService.findPollFormByTypeGestionAndPeriod(PollFormType.CAREERMANAGER_POLLFORM, gestion, period, new Date()));
            if (getPollForm() != null) {
                List<AcademicCareerManagerPlanning> academicStudentPlanningList = academicCareerManagerPlanningService.getPlanning(code, gestion, period);

                if (ValidatorUtil.isEmptyOrNull(academicStudentPlanningList)) {
                    enabledToEvaluate = false;
                    return;
                }

                setEvaluator(academicStructureService.synchronizeEmployee(academicStudentPlanningList.get(0)));

                List<AcademicEmployeePlanning> academicEmployeePlanningList = academicEmployeePlanningService.getPlanningByAcademicCareerManagerPlanning(academicStudentPlanningList);

                for (AcademicEmployeePlanning planning : academicEmployeePlanningList) {
                    AcademicStructure academicStructure = academicStructureService.syncAcademicEmployeeStructure(planning);
                    getAcademicStructureList().add(academicStructure);
                }

                if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
                    enabledToEvaluate = false;
                }
            }

        }
    }
}
