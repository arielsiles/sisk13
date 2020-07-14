package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Entity for accounts
 *
 * @author:
 */

@NamedQueries(
        {

        }
)
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Guarantor.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "garante",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "garante")
public class Guarantor implements BaseModel {

    @Id
    @Column(name = "idgarante", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Guarantor.tableGenerator")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsocio", nullable = false)
    private Partner partner;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
}
