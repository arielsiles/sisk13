package com.encens.khipus.model.admin;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * CompanyModule entity
 *
 * @author
 * @version 1.0.18
 */
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "modulocompania")
public class CompanyModule implements BaseModel {

    @EmbeddedId
    private CompanyModulePk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmodulo", nullable = false, updatable = false, insertable = false)
    private SystemModule systemModule;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    public CompanyModule() {
    }

    public CompanyModulePk getId() {
        return id;
    }

    public void setId(CompanyModulePk id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public SystemModule getSystemModule() {
        return systemModule;
    }

    public void setSystemModule(SystemModule systemModule) {
        this.systemModule = systemModule;
    }
}
