package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.2
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "InventoryDetailLog.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inventario_detalle_log",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "inventario_detalle_log", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class InventoryDetailLog implements BaseModel {
    @Id
    @Column(name = "ID_INVENTARIO_DETALLE_LOG", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InventoryDetailLog.tableGenerator")
    private Long id;

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "ID_INV_DET_ORIGEN", nullable = false)
    private Long sourceInventoryDetailId;

    @Column(name = "ID_INV_DET_DESTINO", nullable = false)
    private Long targetInventoryDetailId;

    @Column(name = "NO_USR", nullable = false, length = 4)
    @Length(max = 4)
    private String userNumber;

    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "DESCRIPCION", nullable = false, length = 250)
    @Length(max = 250)
    private String description;


    @Column(name = "CANTIDAD", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceInventoryDetailId() {
        return sourceInventoryDetailId;
    }

    public void setSourceInventoryDetailId(Long sourceInventoryDetailId) {
        this.sourceInventoryDetailId = sourceInventoryDetailId;
    }

    public Long getTargetInventoryDetailId() {
        return targetInventoryDetailId;
    }

    public void setTargetInventoryDetailId(Long targetInventoryDetailId) {
        this.targetInventoryDetailId = targetInventoryDetailId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}

