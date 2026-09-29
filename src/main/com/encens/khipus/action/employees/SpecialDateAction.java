package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.service.employees.SpecialDateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.List;

/**
 * SpecialDate action class
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("specialDateAction")
@Scope(ScopeType.CONVERSATION)
public class SpecialDateAction extends GenericAction<SpecialDate> {

    @In
    private SpecialDateService specialDateService;
    @In(required = false, scope = ScopeType.EVENT)
    @Out(required = false, scope = ScopeType.EVENT)
    private Boolean specialDateReadOnly;

    @Factory(value = "specialDate", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SPECIALDATE','VIEW')}")
    public SpecialDate initSpecialDate() {
        return getInstance();
    }

    @Create
    public void atCreate() {
        getInstance().setAllDay(true);
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SPECIALDATE','CREATE')}")
    public String create() {
        return super.create();
    }

    @Override
    public String getDisplayNameProperty() {
        return "title";
    }

    @Factory("specialDateRol")
    public SpecialDateRol[] getSpecialDateRol() {
        return SpecialDateRol.values();
    }

    @Factory(value = "specialDateTargetEnum")
    public SpecialDateTarget[] getDestinySpecialDate() {
        return SpecialDateTarget.values();
    }

    @Factory(value = "specialDateType")
    public SpecialDateType[] getExperienceType() {
        return SpecialDateType.values();
    }

    @Factory(value = "specialDateReason")
    public SpecialDateReason[] getSpecialDateReason() {
        return SpecialDateReason.values();
    }

    /**
     * Los motivos que se pueden elegir a mano. Deja fuera VACATION: esa fecha especial la
     * genera el submodulo de vacaciones, que ademas descuenta los dias del saldo. Cargada a
     * mano quedaria un dia pagado sin respaldo en el kardex y el saldo mentiria en silencio.
     * En la lista se sigue viendo y se puede filtrar por el.
     */
    @Factory(value = "specialDateReasonSelectable")
    public List<SpecialDateReason> getSelectableSpecialDateReason() {
        List<SpecialDateReason> selectable = new ArrayList<SpecialDateReason>();
        for (SpecialDateReason reason : SpecialDateReason.values()) {
            /* El compensatorio queda afuera por el mismo motivo que la vacacion: lo genera el
               banco de horas, que ademas descuenta del saldo. Cargado a mano quedaria un dia
               justificado sin respaldo y el saldo mentiria en silencio. */
            if (!SpecialDateReason.VACATION.equals(reason)
                    && !SpecialDateReason.COMPENSATORY.equals(reason)) {
                selectable.add(reason);
            }
        }
        return selectable;
    }

    public Boolean isTargetEmployee(SpecialDate specialDate) {
        return SpecialDateTarget.EMPLOYEE.equals(specialDate.getSpecialDateTarget());
    }

    public Boolean isTargetOrganizationalUnit(SpecialDate specialDate) {
        return SpecialDateTarget.ORGANIZATIONALUNIT.equals(specialDate.getSpecialDateTarget());
    }

    public Boolean isTargetBusinessUnit(SpecialDate specialDate) {
        return SpecialDateTarget.BUSINESSUNIT.equals(specialDate.getSpecialDateTarget());
    }

    public String getOwnerFullName(SpecialDate specialDate) {
        if (isTargetEmployee(specialDate)) {
            return specialDate.getEmployee() != null ? specialDate.getEmployee().getFullName() : null;
        } else if (isTargetOrganizationalUnit(specialDate)) {
            return specialDate.getOrganizationalUnit() != null ? specialDate.getOrganizationalUnit().getName() : null;
        } else if (isTargetBusinessUnit(specialDate)) {
            return specialDate.getBusinessUnit() != null ? specialDate.getBusinessUnit().getPublicity() : null;
        }
        return null;
    }

    public SpecialDateService getSpecialDateService() {
        return specialDateService;
    }

    public void setSpecialDateService(SpecialDateService specialDateService) {
        this.specialDateService = specialDateService;
    }

    public void assignEmployee(Employee employee) {
        getInstance().setEmployee(employee);
    }

    public String getOrganizationalUnitName() {
        return getInstance().getOrganizationalUnit() != null ? getInstance().getOrganizationalUnit().getName() : null;
    }

    public void assingOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        getInstance().setOrganizationalUnit(organizationalUnit);
    }

    public void clearOrganizationalUnit() {
        getInstance().setOrganizationalUnit(null);
    }

    public void clearEmployee() {
        getInstance().setEmployee(null);
    }

    public Boolean getReadOnly() {
        if (specialDateReadOnly == null) {
            specialDateReadOnly = isManaged() && getInstance().getVacation() != null;
        }
        return specialDateReadOnly;
    }
}