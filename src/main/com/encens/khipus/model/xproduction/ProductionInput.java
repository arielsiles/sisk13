package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.production.MetaProduct;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
@NamedQueries({
        @NamedQuery(name = "ProductionInput.findByCode",
                query = "SELECT p FROM ProductionInput p WHERE p.code =:code")
})

@Entity
@Table(name = "insumoproduccion")
@DiscriminatorValue("insumoproduccion")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idinsumoproduccion", referencedColumnName = "idmetaproductoproduccion")})
public class ProductionInput extends MetaProduct {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania" , nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }
}
