package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.0
 */
@Entity
@Table(name = "inv_invmes", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class InventoryHistory implements BaseModel {

    @EmbeddedId
    private InventoryHistoryPK id = new InventoryHistoryPK();

    @Column(name = "UNIDAD_ENT", precision = 12, scale = 2, nullable = false)
    private BigDecimal incomingQuantity;

    @Column(name = "UNIDAD_SAL", precision = 12, scale = 2, nullable = false)
    private BigDecimal outgoingQuantity;

    @Column(name = "MONTO_ENT", precision = 16, scale = 6, nullable = false)
    private BigDecimal incomingAmount;

    @Column(name = "MONTO_SAL", precision = 16, scale = 6, nullable = false)
    private BigDecimal outgoingAmount;

    @Version
    @Column(name = "version")
    private long version;


    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }


    public InventoryHistoryPK getId() {
        return id;
    }

    public void setId(InventoryHistoryPK id) {
        this.id = id;
    }

    public BigDecimal getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(BigDecimal incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
    }

    public BigDecimal getOutgoingQuantity() {
        return outgoingQuantity;
    }

    public void setOutgoingQuantity(BigDecimal outgoingQuantiti) {
        this.outgoingQuantity = outgoingQuantiti;
    }

    public BigDecimal getIncomingAmount() {
        return incomingAmount;
    }

    public void setIncomingAmount(BigDecimal incomingAmount) {
        this.incomingAmount = incomingAmount;
    }

    public BigDecimal getOutgoingAmount() {
        return outgoingAmount;
    }

    public void setOutgoingAmount(BigDecimal outgoingAmount) {
        this.outgoingAmount = outgoingAmount;
    }
}
