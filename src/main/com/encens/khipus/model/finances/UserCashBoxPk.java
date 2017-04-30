package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Primary key class for UserCashBox
 *
 * @author:
 */

@Embeddable
public class UserCashBoxPk implements Serializable {

    @Column(name = "idusuario", nullable = false)
    private Long userId;

    @Column(name = "idcaja", nullable = false)
    private Long cashBoxId;

    public UserCashBoxPk() {
    }

    public UserCashBoxPk(Long userId, Long cashBoxId) {
        this.userId = userId;
        this.cashBoxId = cashBoxId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCashBoxId() {
        return cashBoxId;
    }

    public void setCashBoxId(Long cashBoxId) {
        this.cashBoxId = cashBoxId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserCashBoxPk that = (UserCashBoxPk) o;

        if (cashBoxId != null ? !cashBoxId.equals(that.cashBoxId) : that.cashBoxId != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (cashBoxId != null ? cashBoxId.hashCode() : 0);
        return result;
    }
}
