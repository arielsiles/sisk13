package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AdministrativeGestionPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "gestionplanillaadm",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "AdministrativeGestionPayroll.findByConfigurationTaxPayroll",
                query = "select administrativeGestionPayroll from AdministrativeGestionPayroll administrativeGestionPayroll where administrativeGestionPayroll.configuration =:configurationTaxPayroll"),
        @NamedQuery(name = "AdministrativeGestionPayroll.findIdsByConfigurationTaxPayroll",
                query = "select administrativeGestionPayroll.id from AdministrativeGestionPayroll administrativeGestionPayroll where administrativeGestionPayroll.configuration =:configurationTaxPayroll")
})

@Entity
@Table(name = "GESTIONPLANILLAADM", schema = Constants.KHIPUS_SCHEMA)
public class AdministrativeGestionPayroll implements BaseModel {
    @Id
    @Column(name = "IDGESTIONPLANILLAADM", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AdministrativeGestionPayroll.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCONFPLANILLAFISCAL", insertable = true, updatable = false)
    private ConfigurationTaxPayroll configuration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDGESTIONPLANILLA", insertable = true, updatable = false)
    private GestionPayroll administrativeGestionPayroll;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConfigurationTaxPayroll getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ConfigurationTaxPayroll configuration) {
        this.configuration = configuration;
    }

    public GestionPayroll getAdministrativeGestionPayroll() {
        return administrativeGestionPayroll;
    }

    public void setAdministrativeGestionPayroll(GestionPayroll administrativeGestionPayroll) {
        this.administrativeGestionPayroll = administrativeGestionPayroll;
    }
}
