package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.OutputProductionVoucher;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;


@TableGenerator(name = "IncomingProductionOrder_Generator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "entradaordenproduccion",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "entradaordenproduccion")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class IncomingProductionOrder implements BaseModel {

    @Id
    @Column(name = "identradaordenproduccion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "IncomingProductionOrder_Generator")
    private Long id;

    @Column(name = "observacionentrega", nullable = true, length = 1500)
    private String deliveredObservation;

    @Column(name = "observacionrecepcion", nullable = true, length = 1500)
    private String receivedObservation;

    @Column(name = "cantidadentregada", nullable = false, columnDefinition = "DECIMAL(24,0)")
    private double deliveredAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idinventarioproductoterminado", nullable = true, updatable = true, insertable = true)
    private FinishedGoodsInventory finishedGoodsInventory;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, mappedBy = "incomingProductionOrder")
    private OutputProductionVoucher outputProductionVoucher;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idregistrotransferenciaproduct", nullable = false, updatable = false, insertable = true)
    private ProductionTransferLog productionTransferLog;

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

    public FinishedGoodsInventory getFinishedGoodsInventory() {
        return finishedGoodsInventory;
    }

    public void setFinishedGoodsInventory(FinishedGoodsInventory finishedGoodsInventory) {
        this.finishedGoodsInventory = finishedGoodsInventory;
    }

    public String getDeliveredObservation() {
        return deliveredObservation;
    }

    public void setDeliveredObservation(String deliveredObservation) {
        this.deliveredObservation = deliveredObservation;
    }

    public String getReceivedObservation() {
        return receivedObservation;
    }

    public void setReceivedObservation(String receivedObservation) {
        this.receivedObservation = receivedObservation;
    }

    public double getDeliveredAmount() {
        return deliveredAmount;
    }

    public void setDeliveredAmount(double deliveredAmount) {
        this.deliveredAmount = deliveredAmount;
    }

    public OutputProductionVoucher getOutputProductionVoucher() {
        return outputProductionVoucher;
    }

    public void setOutputProductionVoucher(OutputProductionVoucher outputProductionVoucher) {
        this.outputProductionVoucher = outputProductionVoucher;
    }

    public ProductionTransferLog getProductionTransferLog() {
        return productionTransferLog;
    }

    public void setProductionTransferLog(ProductionTransferLog productionTransferLog) {
        this.productionTransferLog = productionTransferLog;
    }
}
