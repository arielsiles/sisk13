package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.BonusType;
import com.encens.khipus.model.employees.SeniorityBonus;
import com.encens.khipus.model.employees.SeniorityBonusDetail;
import com.encens.khipus.service.employees.BonusService;
import com.encens.khipus.service.employees.TaxPayrollUtilService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.encens.khipus.framework.action.Outcome.FAIL;

/**
 * Bonus action class
 *
 * @author
 * @version 2.26
 */
@Name("bonusAction")
@Scope(ScopeType.CONVERSATION)
public class BonusAction extends GenericAction<Bonus> {

    @In
    private BonusService bonusService;
    @In
    private TaxPayrollUtilService taxPayrollUtilService;

    private boolean readOnlyActive;

    private Date startDate;
    private Date endDate;
    private String description;
    private List<SeniorityBonusDetail> seniorityBonusDetails = new ArrayList<SeniorityBonusDetail>();

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Factory(value = "bonus")
    @Restrict("#{s:hasPermission('BONUS','VIEW')}")
    public Bonus initBonus() {
        if (isManaged()) {
            description = getInstance().getDescription() != null ? getInstance().getDescription().getValue() : null;
            if (getInstance().getBonusType() == BonusType.SENIORITY_BONUS) {
                SeniorityBonus seniorityBonus = (SeniorityBonus) getInstance();
                startDate = seniorityBonus.getStartDate();
                endDate = seniorityBonus.getEndDate();
                seniorityBonusDetails = seniorityBonus.getDetails();
                readOnlyActive = seniorityBonus.getActive();
            }
        }
        return getInstance();
    }

    @Override
    protected GenericService getService() {
        return bonusService;
    }

    @Override
    @End
    public String create() {
        if (description != null) {
            getInstance().setDescription(new Text(description));
        }

        if (getInstance().getBonusType() == BonusType.SENIORITY_BONUS) {
            Bonus oldInstance = getInstance();
            SeniorityBonus newInstace = new SeniorityBonus();
            newInstace.setName(oldInstance.getName());
            newInstace.setDescription(oldInstance.getDescription());
            newInstace.setActive(oldInstance.getActive());
            newInstace.setBonusType(oldInstance.getBonusType());
            newInstace.setStartDate(startDate);
            newInstace.setEndDate(endDate);
            newInstace.setDetails(seniorityBonusDetails);
            newInstace.setSmnRate(taxPayrollUtilService.getActiveSmnRate());
            setInstance(newInstace);
        }
        return super.create();
    }

    @Override
    @End
    public String update() {
        if (description != null) {
            getInstance().setDescription(new Text(description));
        } else {
            getInstance().setDescription(null);
        }
        if (getInstance().getBonusType() == BonusType.SENIORITY_BONUS) {
            SeniorityBonus seniorityBonus = (SeniorityBonus) getInstance();
            seniorityBonus.setStartDate(startDate);
            seniorityBonus.setEndDate(endDate);
            seniorityBonus.setDetails(seniorityBonusDetails);
        }

        return super.update();
    }

    @End
    public String delete() {
        if (isSeniorityBonus() && getInstance().getActive()) {
            log.debug("entity cannot be deleted because is the active rate");
            addDeleteActiveMessage();

            return FAIL;
        } else {
            return super.delete();
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SeniorityBonusDetail> getSeniorityBonusDetails() {
        return seniorityBonusDetails;
    }

    public void setSeniorityBonusDetails(List<SeniorityBonusDetail> seniorityBonusDetails) {
        this.seniorityBonusDetails = seniorityBonusDetails;
    }

    public boolean isSeniorityBonus() {
        return getInstance().getBonusType() == BonusType.SENIORITY_BONUS;
    }

    protected void addDeleteActiveMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.activeObject.delete", messages.get("Common.info.item"));
    }

    public boolean getReadOnlyActive() {
        return readOnlyActive;
    }

    public void bonusTypeChangeAction() {
        seniorityBonusDetails.clear();
        if (isSeniorityBonus()) {
            getInstance().setSmnRate(taxPayrollUtilService.getActiveSmnRate());
        } else {
            getInstance().setSmnRate(null);
        }
    }
}
