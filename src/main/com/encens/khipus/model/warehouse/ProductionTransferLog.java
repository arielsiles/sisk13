package com.encens.khipus.model.warehouse;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TableGenerator(name = "ProductionTransferLog_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "registrotransferenciaproduct",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "registrotransferenciaproduct")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class ProductionTransferLog implements BaseModel {

    @Id
    @Column(name = "idregistrotransferenciaproduct", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductionTransferLog_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "fechaentrega", nullable = false, columnDefinition = "DATE")
    private Date deliveredDate;

    @Column(name = "fecharecepcion", nullable = true, columnDefinition = "DATE")
    private Date receivedDate;

    @Column(name = "estado", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductionTransferLogState state;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productionTransferLog", cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<IncomingProductionOrder> incomingProductionOrderList = new ArrayList<IncomingProductionOrder>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<IncomingProductionOrder> getIncomingProductionOrderList() {
        return incomingProductionOrderList;
    }

    public void setIncomingProductionOrderList(List<IncomingProductionOrder> incomingProductionOrderList) {
        this.incomingProductionOrderList = incomingProductionOrderList;
    }

    public ProductionTransferLogState getState() {
        return state;
    }

    public void setState(ProductionTransferLogState state) {
        this.state = state;
    }
}
