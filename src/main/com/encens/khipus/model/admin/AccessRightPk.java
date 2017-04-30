package com.encens.khipus.model.admin;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key class for AccessRight
 *
 * @author:
 */

@Embeddable
public class AccessRightPk implements Serializable {

    @Column(name = "idfuncionalidad", nullable = false, updatable = false)
    private Long functionId;

    @Column(name = "idrol", nullable = false, updatable = false)
    private Long roleId;

    public AccessRightPk() {
    }

    public AccessRightPk(Long functionId, Long roleId) {
        this.functionId = functionId;
        this.roleId = roleId;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessRightPk that = (AccessRightPk) o;

        if (functionId != null ? !functionId.equals(that.functionId) : that.functionId != null) {
            return false;
        }
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (functionId != null ? functionId.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        return result;
    }
}

