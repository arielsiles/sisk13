package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.academics.Horary;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBand;
import com.encens.khipus.model.employees.HoraryBandContract;
import com.encens.khipus.model.employees.HoraryBandTimeType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.academics.HoraryService;
import com.encens.khipus.service.employees.HoraryBandContractService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DayMap;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Actions for HoraryBandContract
 *
 * @author
 * @version 1.1.0
 */

@Name("horaryBandContractAction")
@Scope(ScopeType.CONVERSATION)
public class HoraryBandContractAction extends GenericAction<HoraryBandContract> {

    private Boolean unsubscribeOperation = Boolean.FALSE;
    private Boolean bandChangeOperation = Boolean.FALSE;
    private Date unsubscribeDate;
    private HoraryBand horaryBand = new HoraryBand();
    private HoraryBandContract changeHoraryBandContract;
    private Employee changeEmployee;
    private JobContract changeJobContract;

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private HoraryBandContractService horaryBandContractService;
    @In
    private HoraryService horaryService;

    @Override
    protected String getDisplayNameMessage() {
        return messages.get("HoraryBandContract.title");
    }

    public Boolean getUnsubscribeOperation() {
        return unsubscribeOperation;
    }

    public void setUnsubscribeOperation(Boolean unsubscribeOperation) {
        this.unsubscribeOperation = unsubscribeOperation;
    }

    public Boolean getBandChangeOperation() {
        return bandChangeOperation;
    }

    public void setBandChangeOperation(Boolean bandChangeOperation) {
        this.bandChangeOperation = bandChangeOperation;
    }

    public Date getUnsubscribeDate() {
        return unsubscribeDate;
    }

    public void setUnsubscribeDate(Date unsubscribeDate) {
        this.unsubscribeDate = unsubscribeDate;
    }

    public HoraryBandContract getChangeHoraryBandContract() {
        return changeHoraryBandContract;
    }

    public void setChangeHoraryBandContract(HoraryBandContract changeHoraryBandContract) {
        this.changeHoraryBandContract = changeHoraryBandContract;
    }

    public Employee getChangeEmployee() {
        return changeEmployee;
    }

    public void setChangeEmployee(Employee changeEmployee) {
        this.changeEmployee = changeEmployee;
    }

    public String getChangeEmployeeFullName() {
        return getChangeEmployee() != null ? getChangeEmployee().getFullName() : null;
    }

    public void assignEmployee(Employee employee) {
        setChangeEmployee(employee);
    }

    public void clearChangeEmployee() {
        setChangeEmployee(null);
    }

    public JobContract getChangeJobContract() {
        return changeJobContract;
    }

    public void setChangeJobContract(JobContract changeJobContract) {
        this.changeJobContract = changeJobContract;
    }

    public HoraryBand getHoraryBand() {
        return horaryBand;
    }

    public void setHoraryBand(HoraryBand horaryBand) {
        this.horaryBand = horaryBand;
    }

    public void assignJobContract(JobContract jobContract) {
        try {
            jobContract = genericService.findById(JobContract.class, jobContract.getId());
            if (jobContract != null) {
                getInstance().setLimit(jobContract.getJob().getJobCategory().getLimit());
                getInstance().setTolerance(jobContract.getJob().getJobCategory().getTolerance());
            }
        } catch (EntryNotFoundException e) {
        } catch (Exception e) {
            log.error(e, "An unexpected error has happened.");
        }
        getInstance().setJobContract(jobContract);
        setChangeEmployee(jobContract.getContract().getEmployee());

    }

    public void clearJobContract() {
        getInstance().setJobContract(null);
        setChangeEmployee(null);
        getInstance().setLimit(null);
        getInstance().setTolerance(null);
    }

    public String getJobFullName() {
        return getInstance().getJobContract() != null ? getInstance().getJobContract().getJob().getCharge().getName() : null;
    }

    public String getOrganizationalUnitFullName() {
        return getInstance().getJobContract() != null ? getInstance().getJobContract().getJob().getOrganizationalUnit().getName() : null;
    }

    @Factory(value = "horaryBandContract", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('HORARYBANDCONTRACT','VIEW')}")
    public HoraryBandContract initHoraryBandContract() {
        return getInstance();
    }

    @Factory(value = "horaryBandTimeTypeEnum")
    public HoraryBandTimeType[] getHoraryBandTimeTypeEnum() {
        return HoraryBandTimeType.values();
    }

//    @Override
//    protected String getDisplayNameProperty() {
//        return "initDate";
//    }

    @Override
    @End
    @Restrict("#{s:hasPermission('HORARYBANDCONTRACT','CREATE')}")
    public String create() {
        if (getInstance().getJobContract() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.horaryBandToleranceRequired");
            return Outcome.REDISPLAY;
        }

        if (getHoraryBand().getDuration() > 0 && getHoraryBand().getDuration() % 45 > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.duration");
            return Outcome.REDISPLAY;
        }
        if (DayMap.dayStringToInt(getHoraryBand().getEndDay()) < DayMap.dayStringToInt(getHoraryBand().getInitDay())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.endDayLessThanInitDay");
            return Outcome.REDISPLAY;
        }
        if (horaryBandContractService.checkOverlapWithoutReference(
                getChangeEmployee().getIdNumber(),
                getInstance().getInitDate(),
                getInstance().getEndDate(),
                getHoraryBand().getInitHour(),
                getHoraryBand().getEndHour(),
                getHoraryBand().getInitDay(),
                getHoraryBand().getEndDay())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.overlap");
            return Outcome.REDISPLAY;
        }

        if (!(horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getInstance().getInitDate())
                && horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getInstance().getEndDate()))) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.dateOutOfContratRange", getChangeEmployee().getFullName());
            return Outcome.REDISPLAY;
        }

        try {
            horaryBandContractService.create(getInstance(), getHoraryBand());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('HORARYBANDCONTRACT','UPDATE')}")
    public String update() {

        if (getInstance().getJobContract() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.horaryBandToleranceRequired");
            return Outcome.REDISPLAY;
        }

        if (getHoraryBand().getDuration() > 0 && getHoraryBand().getDuration() % Constants.PIRIOD.intValue() > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.duration");
            return Outcome.REDISPLAY;
        }

        if (horaryBandContractService.checkOverlap(getInstance().getId(), getChangeEmployee().getIdNumber(),
                getInstance().getInitDate(),
                getInstance().getEndDate(),
                getHoraryBand().getInitHour(),
                getHoraryBand().getEndHour(),
                getHoraryBand().getInitDay(),
                getHoraryBand().getEndDay())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.overlap");
            return Outcome.REDISPLAY;
        }

        if (!(horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getInstance().getInitDate())
                && horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getInstance().getEndDate()))) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.dateOutOfContratRange", getChangeEmployee().getFullName());
            return Outcome.REDISPLAY;
        }

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            horaryBandContractService.update(getInstance(), getHoraryBand());
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
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    public String delete() {
        try {
            horaryBandContractService.delete(getInstance(), getHoraryBand());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('HORARYBANDCONTRACT','VIEW')}")
    public String select(HoraryBandContract instance) {
        String outcome = super.select(instance);
        setChangeEmployee(getInstance().getJobContract().getContract().getEmployee());
        setHoraryBand(getInstance().getHoraryBand());
        return outcome;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String selectUnsubscribe(HoraryBandContract instance) {
        setUnsubscribeOperation(true);
        return super.select(instance);

    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String selectBandChange(HoraryBandContract instance) {
        String outcome = super.select(instance);
        setBandChangeOperation(true);
        setChangeHoraryBandContract(new HoraryBandContract(getInstance()));
        getChangeHoraryBandContract().setInitDate(null);
        getChangeHoraryBandContract().setEndDate(null);
        setChangeEmployee(getInstance().getJobContract().getContract().getEmployee());
        setChangeJobContract(getInstance().getJobContract());
        return outcome;

    }

    @End
    public String unsubscribe() {

        if (!horaryBandContractService.checkContractRange(getInstance().getJobContract().getContract().getEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getUnsubscribeDate())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.unsubscribeDateOutOfContratRange", getInstance().getJobContract().getContract().getEmployee().getFullName());
            return Outcome.REDISPLAY;
        }

        getInstance().setEndDate(getUnsubscribeDate());
        return super.update();
    }

    @End
    public String bandChange() {

        if (horaryBandContractService.checkOverlap(getInstance().getId(), getChangeEmployee().getIdNumber(),
                getChangeHoraryBandContract().getInitDate(),
                getChangeHoraryBandContract().getEndDate(),
                getChangeHoraryBandContract().getHoraryBand().getInitHour(),
                getChangeHoraryBandContract().getHoraryBand().getEndHour(),
                getChangeHoraryBandContract().getHoraryBand().getInitDay(),
                getChangeHoraryBandContract().getHoraryBand().getEndDay())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.overlap");
            return Outcome.REDISPLAY;
        }

        if (!(horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getChangeHoraryBandContract().getInitDate())
                && horaryBandContractService.checkContractRange(getChangeEmployee(), getInstance().getJobContract().getJob().getOrganizationalUnit(), getChangeHoraryBandContract().getEndDate()))) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HoraryBandContract.error.dateOutOfContratRange", getChangeEmployee().getFullName());
            return Outcome.REDISPLAY;
        }

        try {

            getInstance().setEndDate(getUnsubscribeDate());
            genericService.update(getInstance());

            HoraryBand horaryBand = getChangeHoraryBandContract().getHoraryBand();
            genericService.create(horaryBand);

            System.out.println("horaryBand  OK with id = " + horaryBand.getId());

            getChangeHoraryBandContract().setJobContract(getChangeJobContract());
            genericService.create(getChangeHoraryBandContract());

            System.out.println("getChangeHoraryBandContract  OK with id = " + getChangeHoraryBandContract().getId());

            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HoraryBandContract.info.successOperation");

        } catch (Exception e) {
            unexpectedErrorLog(e);
        }


        return Outcome.SUCCESS;
    }

    public List<JobContract> getJobContractList() {
        if (getChangeEmployee() != null && getChangeEmployee().getId() != null) {
            try {
                return em.createNamedQuery("JobContract.findJobContractByEmployeeAndOrgUnit")
                        .setParameter("employee", getChangeEmployee())
                        .setParameter("organizationalUnit", getInstance().getJobContract().getJob().getOrganizationalUnit())
                        .getResultList();
            } catch (Exception e) {
            }
        }

        return new ArrayList<JobContract>();
    }

    public String findCareer(Long horaryId, HoraryBandContract horaryBandContract) {
        Horary horary = horaryService.getHoraryById(horaryId, horaryBandContract.getGestion(), horaryBandContract.getPeriod());
        return null != horary ? horary.getCarrer().getName() : horaryBandContract.getJobContract().getJob().getOrganizationalUnit().getName();
    }
}
