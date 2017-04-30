package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnitType;
import com.encens.khipus.service.admin.BusinessUnitTypeService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * Created by IntelliJ IDEA.
 * User: macmac
 * Date: 17-dic-2008
 * Time: 16:50:45
 * To change this template use File | Settings | File Templates.
 */

@Name("businessUnitTypeAction")
@Scope(ScopeType.CONVERSATION)
public class BusinessUnitTypeAction extends GenericAction<BusinessUnitType> {
    @In
    BusinessUnitTypeService businessUnitTypeService;

    @Factory(value = "businessUnitType", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BUSINESSUNITTYPE','VIEW')}")
    public BusinessUnitType initBusinessUnitType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Restrict("#{s:hasPermission('BUSINESSUNITTYPE','CREATE')}")
    public String create() {
        if (Boolean.TRUE.equals(getInstance().getMain())) {
            long mainCount = businessUnitTypeService.countMainBusinessUnitType();
            if (mainCount > 0) {
                /*return can not be more than one main businessUnitType*/
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BusinessUnitType.error.onlyOneMainAllowed");
                return Outcome.REDISPLAY;
            }
        } else {
            getInstance().setMain(Boolean.FALSE);
        }
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('BUSINESSUNITTYPE','UPDATE')}")
    public String update() {
        if (Boolean.TRUE.equals(getInstance().getMain())) {
            long mainCount = businessUnitTypeService.countMainBusinessUnitType();
            // es principal
            if (businessUnitTypeService.findBusinessUnitType(getInstance().getId()).getMain()) {
                return super.update();
            }
            if (mainCount > 0) {
                /*return can not be more than one main businessUnitType*/
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "BusinessUnitType.error.onlyOneMainAllowed");
                return Outcome.REDISPLAY;
            }
        }
        return super.update();
    }
}
