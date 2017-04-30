package com.encens.khipus.action;

import com.encens.khipus.model.admin.PermissionType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import java.util.Map;

/**
 * Overrides some methods of seam identity
 *
 * @author
 */

@Name("org.jboss.seam.security.identity")
@Scope(value = ScopeType.SESSION)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
@Startup
public class AppIdentity extends Identity {

    @Logger
    private Log log;

    public AppIdentity() {
        super();
    }

    private Map<String, Byte> permissions;

    public void setPermissions(Map<String, Byte> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean hasPermission(Object funcObj, String action) {
        if (permissions == null || permissions.size() == 0) {
            return false;
        }

        String functionality = String.valueOf(funcObj);

        String functionCodeUppercase = functionality.toUpperCase();

        if (!permissions.containsKey(functionCodeUppercase)) {
            return false;
        }

        int assgnedPermissionCode = permissions.get(functionCodeUppercase);

        int permissionCode = PermissionType.valueOf(action).getValue();

        return permissionCode == (permissionCode & assgnedPermissionCode);
    }
}
