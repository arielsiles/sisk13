package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This entity stands for maintenance request for fixed assets.
 *
 * @author
 * @version 2.25
 */

@NamedQueries(
        {
                @NamedQuery(name = "FixedAssetMaintenanceRequest.readWithFixedAsset", query = "SELECT fixedAssetMaintenanceRequest " +
                        " FROM FixedAssetMaintenanceRequest fixedAssetMaintenanceRequest " +
                        " LEFT JOIN FETCH fixedAssetMaintenanceRequest.fixedAssets fixedAsset" +
                        " WHERE fixedAssetMaintenanceRequest.id =:fixedAssetMaintenanceRequestId")
        })

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetMaintenanceRequest.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rol",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "solicitudmantenimiento")
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public class FixedAssetMaintenanceRequest implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetMaintenanceRequest.tableGenerator")
    @Column(name = "idsolmant")
    private Long id;

    @Column(name = "codigo", length = 150, nullable = false)
    @Length(max = 150)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsolicitante", nullable = false, updatable = false, insertable = true)
    private JobContract petitioner;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", updatable = false, insertable = true),
            @JoinColumn(name = "codigocencos", referencedColumnName = "cod_cc", updatable = false, insertable = true)
    })
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idunidadejecutora", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idmotivosolicitud", nullable = false)
    private Text maintenanceReason;

    @Column(name = "fechasolmant", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date requestDate;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetMaintenanceRequestType type;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetMaintenanceRequestState requestState;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idmantenimiento", nullable = true)
    private FixedAssetMaintenance maintenance;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "solmantactivofijo",
            joinColumns = @JoinColumn(name = "idsolmant"),
            inverseJoinColumns = @JoinColumn(name = "idactivo"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    private List<FixedAsset> fixedAssets;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idsolmant", referencedColumnName = "idsolmant", nullable = false)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<FixedAssetMaintenanceRequestStateHistory> stateHistoryList = new ArrayList<FixedAssetMaintenanceRequestStateHistory>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public JobContract getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(JobContract petitioner) {
        this.petitioner = petitioner;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public Text getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(Text maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public FixedAssetMaintenanceRequestType getType() {
        return type;
    }

    public void setType(FixedAssetMaintenanceRequestType type) {
        this.type = type;
    }

    public FixedAssetMaintenanceRequestState getRequestState() {
        return requestState;
    }

    public void setRequestState(FixedAssetMaintenanceRequestState requestState) {
        this.requestState = requestState;
    }

    public FixedAssetMaintenance getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(FixedAssetMaintenance maintenance) {
        this.maintenance = maintenance;
    }

    public List<FixedAsset> getFixedAssets() {
        return fixedAssets;
    }

    public void setFixedAssets(List<FixedAsset> fixedAssets) {
        this.fixedAssets = fixedAssets;
    }

    public List<FixedAssetMaintenanceRequestStateHistory> getStateHistoryList() {
        return stateHistoryList;
    }

    public void setStateHistoryList(List<FixedAssetMaintenanceRequestStateHistory> stateHistoryList) {
        this.stateHistoryList = stateHistoryList;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
