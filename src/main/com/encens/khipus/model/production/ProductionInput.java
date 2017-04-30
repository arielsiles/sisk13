package com.encens.khipus.model.production;

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
@Table(name = "INSUMOPRODUCCION")
@DiscriminatorValue("INSUMOPRODUCCION")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "IDINSUMOPRODUCCION", referencedColumnName = "IDMETAPRODUCTOPRODUCCION")})
public class ProductionInput extends MetaProduct {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA" , nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }
}
