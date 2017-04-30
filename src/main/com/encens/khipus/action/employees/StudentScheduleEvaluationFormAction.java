package com.encens.khipus.action.employees;

import com.encens.khipus.model.academics.AcademicStudentPlanning;
import com.encens.khipus.model.employees.PollFormType;
import com.encens.khipus.service.employees.AcademicStudentPlanningService;
import com.encens.khipus.util.academic.AcademicStructure;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * StudentScheduleEvaluationFormAction
 *
 * @author
 * @version 2.24
 */
@Name("studentScheduleEvaluationFormAction")
@Scope(ScopeType.CONVERSATION)
public class StudentScheduleEvaluationFormAction extends GenericScheduleEvaluationFormAction {

    @In
    private AcademicStudentPlanningService academicStudentPlanningService;

    @Create
    public void init() {
        initEvaluationForm(PollFormType.STUDENT_POLLFORM);

        if (validateRequestParameter()) {
            setPollForm(scheduleEvaluationService.findPollFormByTypeGestionAndPeriod(PollFormType.STUDENT_POLLFORM, gestion, period, new Date()));

            if (getPollForm() != null) {
                List<AcademicStudentPlanning> academicStudentPlanningList = academicStudentPlanningService.getPlanning(code, gestion, period);
                for (Iterator<AcademicStudentPlanning> planningIterator = academicStudentPlanningList.iterator(); planningIterator.hasNext();) {
                    AcademicStudentPlanning academicStudentPlanning = planningIterator.next();
                    AcademicStructure academicStructure = academicStructureService.syncAcademicStudentStructure(academicStudentPlanning);
                    getAcademicStructureList().add(academicStructure);
                    setEvaluator(academicStructure.getStudent());
                }

                if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
                    enabledToEvaluate = false;
                }
            }
        }
    }
}
