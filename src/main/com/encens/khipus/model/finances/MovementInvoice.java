package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Movement invoice
 *
 * @author
 * @version
 */
@NamedQueries({})

/*@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "Voucher.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimiento",
        initialValue = 1,
        allocationSize = 2)*/

@Entity
@Table(name = "movimiento", schema = Constants.FINANCES_SCHEMA)
public class MovementInvoice implements BaseModel{

    @Id
    @Column(name = "idmovimiento", nullable = false)
    private Long id;

    @Column(name = "fecha_factura", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date = new Date();

    @Column(name = "estado")
    private String state;

    @Column(name = "nrofactura", nullable = false)
    private Integer invoiceNumber;


    public MovementInvoice() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}