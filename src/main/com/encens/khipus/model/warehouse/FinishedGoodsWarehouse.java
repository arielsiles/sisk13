package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@TableGenerator(name = "FinishedGoodsWarehouse_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "DEPOSITOPRODUCTOTERMINADO",
        allocationSize = 10)

@Entity
@Table(name = "DEPOSITOPRODUCTOTERMINADO", uniqueConstraints = @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class FinishedGoodsWarehouse implements BaseModel {

    @Id
    @Column(name = "IDDEPOSITOPRODUCTOTERMINADO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FinishedGoodsWarehouse_Generator")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String name;

    @Column(name = "CODIGO", nullable = false, length = 50)
    private String code;

    @Column(name = "DESCRIPCION", nullable = true, length = 1500)
    private String description;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "finishedGoodsWarehouse", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<WarehouseSlot> warehouseSlotList = new ArrayList<WarehouseSlot>();

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<WarehouseSlot> getWarehouseSlotList() {
        return warehouseSlotList;
    }

    public void setWarehouseSlotList(List<WarehouseSlot> warehouseSlotList) {
        this.warehouseSlotList = warehouseSlotList;
    }
}
