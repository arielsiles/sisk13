package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.*;
import com.encens.khipus.model.admin.Role;
import com.encens.khipus.service.admin.AccessRightService;
import com.encens.khipus.service.admin.SystemModuleService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.*;

/**
 * Role action
 *
 * @author
 * @version 1.0
 */
@Name("roleAction")
@Scope(ScopeType.CONVERSATION)
public class RoleAction extends GenericAction<Role> {

    private List<SystemModule> modules;
    private Map<Long, List<SystemFunction>> functionsPerModule;
    private Map<SystemFunction, Map<String, Boolean>> permissionsPerFunction;

    @In
    private AccessRightService accessRightService;
    @In
    private SystemModuleService systemModuleService;
    @In
    private Company currentCompany;

    @Factory(scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ROLE','VIEW')}")
    public Role getRole() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String selectAndAccessRight(Role role) {
        return super.select(role);
    }

    public String assignPermissions() {
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public List<SystemModule> getModules() {
        if (modules == null) {
            functionsPerModule = new HashMap<Long, List<SystemFunction>>();
            permissionsPerFunction = new HashMap<SystemFunction, Map<String, Boolean>>();

            modules = systemModuleService.getCompanyModules(currentCompany, true);
            List<SystemFunction> systemFunctionList = accessRightService.getAllFunctions(currentCompany);
            Map<SystemFunction, AccessRight> accessRightMap = accessRightService.getAccessRightMapByFunction(getInstance());

            for (SystemFunction systemFunction : systemFunctionList) {
                if (!functionsPerModule.containsKey(systemFunction.getModuleId())) {
                    functionsPerModule.put(systemFunction.getModuleId(), new ArrayList<SystemFunction>());
                }
                functionsPerModule.get(systemFunction.getModuleId()).add(systemFunction);
                permissionsPerFunction.put(systemFunction, getPermissions(accessRightMap.get(systemFunction), systemFunction));
            }
        }

        return modules;
    }

    public Map<Long, List<SystemFunction>> getFunctionsPerModule() {
        return functionsPerModule;
    }

    public void setFunctionsPerModule(Map<Long, List<SystemFunction>> functionsPerModule) {
        this.functionsPerModule = functionsPerModule;
    }

    public Map<SystemFunction, Map<String, Boolean>> getPermissionsPerFunction() {
        return permissionsPerFunction;
    }

    public void setPermissionsPerFunction(Map<SystemFunction, Map<String, Boolean>> permissionsPerFunction) {
        this.permissionsPerFunction = permissionsPerFunction;
    }

    private Map<String, Boolean> getPermissions(AccessRight accessRight, SystemFunction function) {
        Map<String, Boolean> permissionMap = new HashMap<String, Boolean>();

        int permission = accessRight != null ? accessRight.getPermission() : 0;

        int permissionToCheck = PermissionType.VIEW.getValue();
        if (permissionToCheck == (permissionToCheck & function.getPermission())) {
            permissionMap.put(PermissionType.VIEW.name(), (permissionToCheck == (permissionToCheck & permission)));
        }

        permissionToCheck = PermissionType.CREATE.getValue();
        if (permissionToCheck == (permissionToCheck & function.getPermission())) {
            permissionMap.put(PermissionType.CREATE.name(), (permissionToCheck == (permissionToCheck & permission)));
        }

        permissionToCheck = PermissionType.UPDATE.getValue();
        if (permissionToCheck == (permissionToCheck & function.getPermission())) {
            permissionMap.put(PermissionType.UPDATE.name(), (permissionToCheck == (permissionToCheck & permission)));
        }

        permissionToCheck = PermissionType.DELETE.getValue();
        if (permissionToCheck == (permissionToCheck & function.getPermission())) {
            permissionMap.put(PermissionType.DELETE.name(), (permissionToCheck == (permissionToCheck & permission)));
        }

        permissionMap.put("ALL", !permissionMap.containsValue(false));

        return permissionMap;
    }

    @End
    @Restrict("#{s:hasPermission('ROLE','UPDATE')}")
    public String setPermissions() {
        Iterator iter = permissionsPerFunction.keySet().iterator();
        Map<SystemFunction, AccessRight> accessRightMap = accessRightService.getAccessRightMapByFunction(getInstance());
        AccessRight accessRight;
        Integer permission = 0;
        Boolean hasPermission;
        while (iter.hasNext()) {
            SystemFunction function = (SystemFunction) iter.next();
            hasPermission = permissionsPerFunction.get(function).get(PermissionType.VIEW.name());
            permission = (hasPermission != null && hasPermission) ? PermissionType.VIEW.getValue() : 0;

            hasPermission = permissionsPerFunction.get(function).get(PermissionType.CREATE.name());
            permission = permission | ((hasPermission != null && hasPermission) ? PermissionType.CREATE.getValue() : 0);

            hasPermission = permissionsPerFunction.get(function).get(PermissionType.UPDATE.name());
            permission = permission | ((hasPermission != null && hasPermission) ? PermissionType.UPDATE.getValue() : 0);

            hasPermission = permissionsPerFunction.get(function).get(PermissionType.DELETE.name());
            permission = permission | ((hasPermission != null && hasPermission) ? PermissionType.DELETE.getValue() : 0);

            //System.out.println("-------- " + function.getCode() + "-" + permission);
            accessRight = accessRightMap.get(function);
            if (accessRight != null) {
                accessRight.setPermission(permission.byteValue());
                accessRightService.update(accessRight);
            } else if (permission > 0) {
                CompanyModule companyModule = systemModuleService.getCompanyModule(new CompanyModulePk(currentCompany.getId(), function.getModule().getId()));
                if (companyModule != null) {
                    accessRight = new AccessRight(function, getInstance(), companyModule);
                    accessRight.setPermission(permission.byteValue());
                    getInstance().getAccessRights().add(accessRight);
                } else {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                            "CompanyModule.unassignedModule", messages.get(function.getModule().getResourceName()));
                    return com.encens.khipus.framework.action.Outcome.REDISPLAY;
                }
            }
        }
        String result = super.update();
        if (result == null) {
            functionsPerModule.clear();
            permissionsPerFunction.clear();
        }
        return result;
    }
}
