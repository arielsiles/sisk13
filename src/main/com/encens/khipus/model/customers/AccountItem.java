package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "CustomerOrder_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "cuentas_art_wise",
        allocationSize = 10)

@Entity
@Table(name = "cuentas_art_wise",schema = Constants.CASHBOX_SCHEMA)
//@Filter(name = "companyFilter")
//@EntityListeners(CompanyListener.class)
public class AccountItem implements BaseModel {

    @EmbeddedId
    private AccountItemPK id = new AccountItemPK();

/*    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "COD_ART", nullable = true,updatable = false,insertable = false)
    private ProductItem productItem;*/

    @Column(name = "cod_alm", nullable = false)
    private String codWarehouse;

    public AccountItemPK getId() {
        return id;
    }

    public void setId(AccountItemPK id) {
        this.id = id;
    }

    public String getCodWarehouse() {
        return codWarehouse;
    }

    public void setCodWarehouse(String codWarehouse) {
        this.codWarehouse = codWarehouse;
    }

/*    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }*/

}
