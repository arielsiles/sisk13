package com.encens.khipus.model.warehouse;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 */
@Embeddable
public class ProductItemPK implements Serializable {

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_ART", nullable = false, length = 6)
    @Length(max = 6)
    private String productItemCode;


    public ProductItemPK() {
    }

    public ProductItemPK(String companyNumber, String productItemCode) {
        this.companyNumber = companyNumber;
        this.productItemCode = productItemCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductItemPK that = (ProductItemPK) o;

        if (!companyNumber.equals(that.companyNumber)) {
            return false;
        }
        if (!productItemCode.equals(that.productItemCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyNumber.hashCode();
        result = 31 * result + productItemCode.hashCode();
        return result;
    }
}
