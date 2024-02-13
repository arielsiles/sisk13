package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Customer categories
 *
 * @author:
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "XMachineProcess.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "xpr_procesomaquina",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "xpr_procesomaquina", uniqueConstraints = @UniqueConstraint(columnNames = {"idprocesomaquina"}))
public class XMachineProcess implements BaseModel {

    @Id
    @Column(name = "idprocesomaquina", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XMachineProcess.tableGenerator")
    private Long id;



    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproceso", nullable = false, updatable = false, insertable = true)
    private XProcess xProcess;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmaquina", nullable = false, updatable = false, insertable = true)
    private XMachine xmachine;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public XProcess getxProcess() {
        return xProcess;
    }

    public void setxProcess(XProcess xProcess) {
        this.xProcess = xProcess;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public XMachine getXmachine() {
        return xmachine;
    }

    public void setXmachine(XMachine xmachine) {
        this.xmachine = xmachine;
    }
}
