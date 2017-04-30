package com.encens.khipus.action.employees;

import com.encens.khipus.model.academics.AcademicEmployeePlanning;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.service.employees.AcademicEmployeePlanningService;
import com.encens.khipus.util.academic.AcademicStructure;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.24
 */

@Name("academicEmployeeScheduleEvaluationFormAction")
@Scope(ScopeType.CONVERSATION)
public class AcademicEmployeeScheduleEvaluationFormAction extends GenericScheduleEvaluationFormAction {

    @In
    private AcademicEmployeePlanningService academicEmployeePlanningService;

    @Create
    public void init() {
        initEvaluationForm(PollFormType.AUTOEVALUATION_POLLFORM);

        if (validateRequestParameter()) {
            setPollForm(scheduleEvaluationService.findPollFormByTypeGestionAndPeriod(PollFormType.AUTOEVALUATION_POLLFORM,
                    gestion,
                    period,
                    new Date()));

            List<AcademicEmployeePlanning> plannings = academicEmployeePlanningService.getPlanningWithinSubject(code, gestion, period);

            for (AcademicEmployeePlanning planning : plannings) {
                AcademicStructure academicStructure = academicStructureService.syncAcademicEmployeeStructure(planning);
                getAcademicStructureList().add(academicStructure);
                setEvaluator(academicStructure.getEmployee());
            }

            if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
                enabledToEvaluate = false;
            }
        }
    }
}
