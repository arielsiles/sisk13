package com.encens.khipus.model.employees;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * PostulantAcademicFormation
 *
 * @author
 * @version 2.25
 */
@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "formacionacadpost")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idformacionacadpost", referencedColumnName = "idformacionacademica")
})
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
public class PostulantAcademicFormation extends AcademicFormation {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idpostulante")
    private Postulant postulant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Postulant getPostulant() {
        return postulant;
    }

    public void setPostulant(Postulant postulant) {
        this.postulant = postulant;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
