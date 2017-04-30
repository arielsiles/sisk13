package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.employees.Dismissal;
import com.encens.khipus.model.employees.DismissalState;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.DismissalService;
import com.encens.khipus.service.employees.JobContractService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author
 * @version 3.4
 */

@Name("dismissalAction")
@Scope(ScopeType.CONVERSATION)
public class DismissalAction extends GenericAction<Dismissal> {

    @In
    private DismissalService dismissalService;

    @In
    private JobContractService jobContractService;

    private Long workedDays;

    private File file = new File();

    @Factory(value = "dismissal", scope = ScopeType.STATELESS)
    public Dismissal init() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return String.valueOf(getInstance().getCode());
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Dismissal instance) {
        String outcome = super.select(instance);
        setFile(getInstance().getFile());
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSAL','CREATE')}")
    public String create() {
        try {
            if (null != file.getValue()) {
                getInstance().setFile(file);
            }
            dismissalService.createDismissal(getInstance());
            addCreatedMessage();
            return com.encens.khipus.framework.action.Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('DISMISSAL','CREATE')}")
    public void createAndNew() {
        try {
            if (null != file.getValue()) {
                getInstance().setFile(file);
            }
            dismissalService.createDismissal(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSAL','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('DISMISSAL','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public void assignContract(JobContract jobContract) {
        try {
            if (jobContract != null && jobContract.getId() != null) {
                getInstance().setContract(getService().findById(JobContract.class, jobContract.getId()).getContract());
            }
        } catch (EntryNotFoundException ignored) {

        }
        calculateWorkedDays();
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearContract() {
        getInstance().setContract(null);
        calculateWorkedDays();
    }

    public boolean isPending() {
        return !isManaged() || (null != getInstance().getState() && DismissalState.PENDING.equals(getInstance().getState()));
    }

    public boolean validateLayOffDate() {
        Contract contract = getInstance().getContract();
        return null == contract || ((contract.getInitDate().compareTo(getInstance().getLayOffDate()) <= 0)
                && (null == contract.getEndDate() || contract.getEndDate().compareTo(getInstance().getLayOffDate()) >= 0));
    }

    public void calculateWorkedDays() {
        Long res = null;
        if (null != getInstance().getContract()) {
            Date endDate = null != getInstance().getLayOffDate() ? getInstance().getLayOffDate() : new Date();
            res = DateUtils.daysBetween(getInstance().getContract().getInitDate(), endDate);
        }
        setWorkedDays(res);
    }

    public Long getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(Long workedDays) {
        this.workedDays = workedDays;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Download file
     */
    public void download() {
        File file = getInstance().getFile();

        HttpServletResponse response = JSFUtil.getHttpServletResponse();
        response.setContentType(file.getContentType());
        response.addHeader("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(file.getValue());
            os.flush();
            os.close();
            JSFUtil.getFacesContext().responseComplete();
        } catch (Exception e) {
            log.error("\nFailure : " + e.toString() + "\n");
        }
    }
}