package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "FinishedGoodsInventory_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "inventarioproductoterminado",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "inventarioproductoterminado")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class FinishedGoodsInventory implements BaseModel {
    @Id
    @Column(name = "idinventarioproductoterminado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FinishedGoodsInventory_Generator")
    private Long id;

    @Column(name = "cantidad", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private Double amount;

    @Column(name = "fecha", nullable = false, columnDefinition = "DATE")
    private Date date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idambientedeposito", nullable = false, updatable = false, insertable = true)
    private WarehouseSlot warehouseSlot;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WarehouseSlot getWarehouseSlot() {
        return warehouseSlot;
    }

    public void setWarehouseSlot(WarehouseSlot warehouseSlot) {
        this.warehouseSlot = warehouseSlot;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
