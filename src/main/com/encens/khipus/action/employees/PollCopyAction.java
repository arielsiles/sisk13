package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.EvaluationCriteriaService;
import com.encens.khipus.service.employees.PollCopyService;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.employees.PollFormUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PollCopyAction
 *
 * @author
 * @version 1.1.6
 */
@Name("pollCopyAction")
@Scope(ScopeType.CONVERSATION)
public class PollCopyAction extends GenericAction<PollCopy> {

    private Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult = new HashMap<Question, EvaluationCriteriaValue>();

    @In(required = false)
    private PollFormAction pollFormAction;
    @In
    private PollFormService pollFormService;
    @In
    private PollCopyService pollCopyService;
    @In
    private EvaluationCriteriaService evaluationCriteriaService;

    private Location location;

    private Faculty faculty;

    private Career career;

    private Subject subject;

    private String fromPollForm = "fromPollForm";

    private String currentFromView = null;

    @Factory(value = "pollCopy", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
    public PollCopy initPollCopy() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return MessageUtils.getMessage("PollCopy.title");
    }

    public String getCurrentFromView() {
        return currentFromView;
    }

    public void setCurrentFromView(String currentFromView) {
        this.currentFromView = currentFromView;
    }

    public Map<Question, EvaluationCriteriaValue> getEvaluationCriteriaValueResult() {
        return evaluationCriteriaValueResult;
    }

    public void setEvaluationCriteriaValueResult(Map<Question, EvaluationCriteriaValue> evaluationCriteriaValueResult) {
        this.evaluationCriteriaValueResult = evaluationCriteriaValueResult;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Career getCareer() {
        return career;
    }

    public void setCareer(Career career) {
        this.career = career;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshLocation() {
        setFaculty(null);
        setCareer(null);
        setSubject(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshFaculty() {
        setCareer(null);
        setSubject(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshCareer() {
        setSubject(null);
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String assignPollCopy() {
        setCurrentFromView(fromPollForm);
        createInstance();
        try {
            getInstance().setPollForm(genericService.findById(PollForm.class, pollFormAction.getInstance().getId()));
        } catch (EntryNotFoundException e) {
            return Outcome.FAIL;
        }
        return Outcome.SUCCESS;
    }

    @Override
    @Restrict("#{s:hasPermission('POLLCOPY','CREATE')}")
    public String create() {

        PollFormUtil.insertGrouppingValues(getInstance(), getFaculty(), getCareer(), getSubject());

        if (PollFormUtil.isRequired(getInstance().getPollForm().getPersonRestriction()) &&
                getInstance().getPerson() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PollCopy.error.personRequired");
            return Outcome.REDISPLAY;
        }

        try {
            pollCopyService.createPollCopy(getInstance(), getEvaluationCriteriaValueResult());
            addCreatedMessage();
            String outcome = getCurrentFromView();
            closeConversation(outcome);
            return outcome;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('POLLCOPY','CREATE')}")
    public void createAndNew() {

        PollFormUtil.insertGrouppingValues(getInstance(), getFaculty(), getCareer(), getSubject());

        if (PollFormUtil.isRequired(getInstance().getPollForm().getPersonRestriction()) &&
                getInstance().getPerson() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PollCopy.error.personRequired");
            return;
        }

        try {
            pollCopyService.createPollCopy(getInstance(), getEvaluationCriteriaValueResult());
            addCreatedMessage();
            setInstance(new PollCopy(getInstance()));
            setEvaluationCriteriaValueResult(new HashMap<Question, EvaluationCriteriaValue>());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }


    @Override
    @Restrict("#{s:hasPermission('POLLCOPY','UPDATE')}")
    public String update() {

        PollFormUtil.insertGrouppingValues(getInstance(), getFaculty(), getCareer(), getSubject());

        if (PollFormUtil.isRequired(getInstance().getPollForm().getPersonRestriction()) &&
                getInstance().getPerson() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "PollCopy.error.personRequired");
            return Outcome.REDISPLAY;
        }
        for (PollPunctuation pollPunctuation : getInstance().getPollPunctuationList()) {
            pollPunctuation.setEvaluationCriteriaValue(evaluationCriteriaValueResult.get(pollPunctuation.getQuestion()));
        }

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            getService().update(getInstance());
            addUpdatedMessage();
            String outcome = getCurrentFromView();
            closeConversation(outcome);
            return outcome;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance())));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return getCurrentFromView();
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }

    }

    @Override
    @Restrict("#{s:hasPermission('POLLCOPY','DELETE')}")
    public String delete() {
        super.delete();
        String outcome = getCurrentFromView();
        closeConversation(outcome);
        return outcome;
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        return getCurrentFromView();
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
    public String selectFromPollForm(PollCopy instance) {
        setCurrentFromView(fromPollForm);
        return select(instance);
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
    public String selectFromPollCopyList(PollCopy instance) {
        String fromPollCopyList = "fromPollCopyList";
        setCurrentFromView(fromPollCopyList);
        return select(instance);
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
    @SuppressWarnings({"NullableProblems"})
    public String select(PollCopy instance) {
        String outcomeResult = super.select(instance);
        evaluationCriteriaValueResult = new HashMap<Question, EvaluationCriteriaValue>();
        for (PollPunctuation pollPunctuation : getInstance().getPollPunctuationList()) {
            evaluationCriteriaValueResult.put(pollPunctuation.getQuestion(), pollPunctuation.getEvaluationCriteriaValue());
        }
        if (getInstance().getSubject() != null) {
            setSubject(getInstance().getSubject());
            setCareer(getInstance().getSubject().getCareer());
            setFaculty(getInstance().getSubject().getCareer().getFaculty());
            setLocation(getInstance().getSubject().getCareer().getFaculty().getLocation());
        } else if (getInstance().getCareer() != null) {
            setCareer(getInstance().getCareer());
            setFaculty(getInstance().getCareer().getFaculty());
            setLocation(getInstance().getCareer().getFaculty().getLocation());
        } else if (getInstance().getFaculty() != null) {
            setCareer(null);
            setFaculty(getInstance().getFaculty());
            setLocation(getInstance().getFaculty().getLocation());
        } else {
            setCareer(null);
            setFaculty(null);
            setLocation(null);
        }

        return outcomeResult;
    }

    public String getPersonFullName() {
        return getInstance().getPerson() != null ? getInstance().getPerson().getFullName() : null;
    }

    public void assignPerson(Person person) {
        getInstance().setPerson(person);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearPerson() {
        getInstance().setPerson(null);
    }

    public List<Location> getLocationList() {
        return pollFormService.getLocationList();
    }

    public List<Faculty> getFacultyList() {
        return pollFormService.getFacultyList(getLocation());
    }

    public List<Career> getCareerList() {
        return pollFormService.getCareerList(getFaculty());
    }

    public List<Subject> getSubjectList() {
        return pollFormService.getSubjectList(getCareer());
    }

    public List<EvaluationCriteria> getEvaluationCriteriaList() {
        return evaluationCriteriaService.getDistinctsOnQuestionsByPollForm(getInstance().getPollForm());
    }
}
