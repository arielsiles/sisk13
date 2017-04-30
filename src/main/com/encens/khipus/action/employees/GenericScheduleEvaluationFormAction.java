package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.academics.AcademicStructureService;
import com.encens.khipus.service.employees.EvaluationCriteriaService;
import com.encens.khipus.service.employees.PollCopyService;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.service.employees.ScheduleEvaluationService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.academic.AcademicStructure;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenericScheduleEvaluationFormAction
 *
 * @author
 * @version 2.24
 */
@Name("genericEvaluationFormAction")
public class GenericScheduleEvaluationFormAction implements Serializable {
    @RequestParameter
    protected Integer code;
    @RequestParameter
    protected Integer gestion;
    @RequestParameter
    protected Integer period;

    private Person evaluator;

    private PollForm pollForm;

    private PollFormType pollFormType;

    protected boolean enabledToEvaluate = true;

    private Text comment = new Text();

    private Map<AcademicStructure, Map<Question, EvaluationCriteriaValue>> evaluationCriteriaValueResult = new HashMap<AcademicStructure, Map<Question, EvaluationCriteriaValue>>() {
        @Override
        public Map<Question, EvaluationCriteriaValue> get(Object key) {
            Map<Question, EvaluationCriteriaValue> currentValue = super.get(key);
            if (currentValue == null) {
                super.put((AcademicStructure) key, currentValue = new HashMap<Question, EvaluationCriteriaValue>());
            }
            return currentValue;
        }
    };

    private Map<Question, EvaluationCriteriaValue> questionEvaluationCriteriaValueResult = new HashMap<Question, EvaluationCriteriaValue>();

    private List<AcademicStructure> academicStructureList = new ArrayList<AcademicStructure>();

    @In
    protected FacesMessages facesMessages;
    @In
    protected Map<String, String> messages;
    @In
    protected ScheduleEvaluationService scheduleEvaluationService;
    @In
    protected EvaluationCriteriaService evaluationCriteriaService;
    @In
    protected AcademicStructureService academicStructureService;
    @In
    protected PollCopyService pollCopyService;
    @In
    protected PollFormService pollFormService;
    @In
    protected CompanyConfigurationService companyConfigurationService;

    public void initEvaluationForm(PollFormType type) {
        setPollFormType(type);
        Contexts.getSessionContext().set("currentCompany", new Company(Constants.defaultCompanyId, Constants.defaultCompanyName));
    }

    public Person getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Person evaluator) {
        this.evaluator = evaluator;
    }

    public PollForm getPollForm() {
        return pollForm;
    }

    public void setPollForm(PollForm pollForm) {
        this.pollForm = pollForm;
    }

    public PollFormType getPollFormType() {
        return pollFormType;
    }

    public void setPollFormType(PollFormType pollFormType) {
        this.pollFormType = pollFormType;
    }

    public Map<AcademicStructure, Map<Question, EvaluationCriteriaValue>> getEvaluationCriteriaValueResult() {
        return evaluationCriteriaValueResult;
    }

    public void setEvaluationCriteriaValueResult(Map<AcademicStructure, Map<Question, EvaluationCriteriaValue>> evaluationCriteriaValueResult) {
        this.evaluationCriteriaValueResult = evaluationCriteriaValueResult;
    }

    public Map<Question, EvaluationCriteriaValue> getQuestionEvaluationCriteriaValueResult() {
        return questionEvaluationCriteriaValueResult;
    }

    public void setQuestionEvaluationCriteriaValueResult(Map<Question, EvaluationCriteriaValue> questionEvaluationCriteriaValueResult) {
        this.questionEvaluationCriteriaValueResult = questionEvaluationCriteriaValueResult;
    }

    public List<AcademicStructure> getAcademicStructureList() {
        return academicStructureList;
    }

    public void setAcademicStructureList(List<AcademicStructure> academicStructureList) {
        this.academicStructureList = academicStructureList;
    }

    public int getAcademicStructureListSize() {
        return getAcademicStructureList().size();
    }

    public List<EvaluationCriteria> getEvaluationCriteriaList() {
        return evaluationCriteriaService.getDistinctsOnQuestionsByPollForm(getPollForm());
    }

    public List<Question> getQuestionList() {
        return pollFormService.getQuestionListByPollForm(getPollForm());
    }

    public Integer getQuestionListSize() {
        return pollFormService.getQuestionListByPollForm(getPollForm()).size();
    }

    public Boolean validateRequestParameter() {
        return null != code && null != gestion && null != period;
    }

    public Boolean validateGroupPunctuations() {
        return !evaluationCriteriaValueResult.isEmpty();
    }

    public Boolean validateUnGroupPunctuations() {
        return !questionEvaluationCriteriaValueResult.isEmpty();
    }

    public boolean isEnabledToEvaluate() {
        return enabledToEvaluate;
    }

    public void setEnabledToEvaluate(boolean enabledToEvaluate) {
        this.enabledToEvaluate = enabledToEvaluate;
    }

    public Text getComment() {
        return comment;
    }

    public void setComment(Text comment) {
        this.comment = comment;
    }

    @End
    public String createByGroupPunctuations() {
        if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
            addUnableMakeEvaluationMessage();
            setRedirectURL();
            return Outcome.FAIL;
        }

        if (!validateGroupPunctuations()) {
            addNoPunctuationsForEvaluationMessage();
            return Outcome.REDISPLAY;
        }

        try {
            pollCopyService.createFormAcademicStructure(getPollForm(), getEvaluator(), getEvaluationCriteriaValueResult(), getComment());
            addCreatedMessage();
            setRedirectURL();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String createByUnGroupPunctuations() {
        if (!pollCopyService.isEnabledToEvaluate(getEvaluator(), getPollForm())) {
            addUnableMakeEvaluationMessage();
            setRedirectURL();
            return Outcome.FAIL;
        }

        if (!validateUnGroupPunctuations()) {
            addNoPunctuationsForEvaluationMessage();
            return Outcome.REDISPLAY;
        }

        try {
            pollCopyService.createFormAcademicStructure(getPollForm(), getEvaluator(), getAcademicStructureList(), getQuestionEvaluationCriteriaValueResult(), getComment());
            addCreatedMessage();
            setRedirectURL();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String cancel() {
        setRedirectURL();
        return Outcome.CANCEL;
    }

    public void setRedirectURL() {
        String redirectURL = null;
        try {
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            if (PollFormType.STUDENT_POLLFORM.equals(getPollFormType())) {
                redirectURL = companyConfiguration.getStudentScheduleEvaluationRedirectURL();
            } else if (PollFormType.TEACHER_POLLFORM.equals(getPollFormType())) {
                redirectURL = companyConfiguration.getTeacherScheduleEvaluationRedirectURL();
            } else if (PollFormType.CAREERMANAGER_POLLFORM.equals(getPollFormType())) {
                redirectURL = companyConfiguration.getCareerManagerScheduleEvaluationRedirectURL();
            } else if (PollFormType.AUTOEVALUATION_POLLFORM.equals(getPollFormType())) {
                redirectURL = companyConfiguration.getAutoEvaluationScheduleEvaluationRedirectURL();
            } else {
                redirectURL = companyConfiguration.getScheduleEvaluationRedirectURL();
            }
        } catch (CompanyConfigurationNotFoundException e) {
        }
        JSFUtil.getHttpSession().setAttribute("redirectURL", redirectURL);
    }

    public String getDefaultRedirectURL() {
        String redirectURL = null;
        try {
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            redirectURL = companyConfiguration.getScheduleEvaluationRedirectURL();
        } catch (CompanyConfigurationNotFoundException e) {
        }
        return redirectURL;
    }

    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "ScheduleEvaluationForm.message.created", messages.get(getPollFormType().getResourceKey()));
    }

    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ScheduleEvaluationForm.message.duplicated", messages.get(getPollFormType().getResourceKey()));
    }

    protected void addUnableMakeEvaluationMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ScheduleEvaluationForm.error.notEnabled", gestion, period);
    }

    protected void addNoPunctuationsForEvaluationMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "ScheduleEvaluationForm.error.noPunctuation");
    }
}
