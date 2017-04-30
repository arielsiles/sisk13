package com.encens.khipus.model.common;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.RevisionEntityListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * RevisionEntityInfo
 *
 * @author
 * @version 2.24
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "RevisionEntityInfo.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "revisionentidad",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "revisionentidad")
@RevisionEntity(RevisionEntityListener.class)
public class RevisionEntityInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RevisionEntityInfo.tableGenerator")
    @Column(name = "idreventidad", nullable = false, updatable = false)
    private Long id;

    @RevisionNumber
    @Column(name = "nrorevision", nullable = false, updatable = false)
    private Long revisionNumber;

    @RevisionTimestamp
    @Column(name = "almacenadoen", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date storedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacenadopor", nullable = false, updatable = false)
    private User storedBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Long revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public Date getStoredOn() {
        return storedOn;
    }

    public void setStoredOn(Date storedOn) {
        this.storedOn = storedOn;
    }

    public User getStoredBy() {
        return storedBy;
    }

    public void setStoredBy(User storedBy) {
        this.storedBy = storedBy;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
