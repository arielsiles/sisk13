package com.encens.khipus.model.finances;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Primary key class for CashBoxRecord
 *
 * @author:
 */

@Embeddable
public class CashBoxRecordPk implements Serializable {

    @Column(name = "idcaja", nullable = false)
    private Long cashBoxId;

    @Column(name = "fechaestado", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date stateDate;

    public CashBoxRecordPk() {
    }

    public CashBoxRecordPk(Long cashBoxId, Date stateDate) {
        this.cashBoxId = cashBoxId;
        this.stateDate = stateDate;
    }

    public Long getCashBoxId() {
        return cashBoxId;
    }

    public void setCashBoxId(Long cashBoxId) {
        this.cashBoxId = cashBoxId;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CashBoxRecordPk that = (CashBoxRecordPk) o;

        if (cashBoxId != null ? !cashBoxId.equals(that.cashBoxId) : that.cashBoxId != null) {
            return false;
        }
        if (stateDate != null ? !stateDate.equals(that.stateDate) : that.stateDate != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (cashBoxId != null ? cashBoxId.hashCode() : 0);
        result = 31 * result + (stateDate != null ? stateDate.hashCode() : 0);
        return result;
    }
}
