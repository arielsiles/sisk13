package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "ClientOrder_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PEDIDOS",
        allocationSize = 10)

@Entity
@Table(name = "PER_INSTS",schema = Constants.CASHBOX_SCHEMA)
//@Filter(name = "companyFilter")
//@EntityListeners(CompanyListener.class)
public class ClientOrder implements BaseModel {

    @Id
    @Column(name = "ID", columnDefinition = "VARCHAR2(20 BYTE)", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ClientOrder_Generator")
    private Long id;

    @Column(name = "TIPO", nullable = true)
    private String type;

    @Column(name = "ACTIVO", nullable = true)
    private String active;

    @Column(name = "HST", nullable = false)
    private String hst;

    @Column(name = "NRO_DOC", nullable = false)
    private String numberDoc;

    @Column(name = "MAIL", nullable = true)
    private String mail;

    @Column(name = "TEL_REF",nullable = true)
    private String referPhone;

    @Column(name = "TDO_COD",nullable = true)
    private String typeDoc;

    @Column(name = "FACTURA",nullable = true)
    private String invoice;

    @Column(name = "SUPERVISOR",nullable = true )
    private String supevisor;

    /*@OneToMany(mappedBy = "clientOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<CustomerOrder> customerOrders = new ArrayList<CustomerOrder>();*/


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID", nullable = false, updatable = false, insertable = false)
    private ClientPerson clientPerson;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID", nullable = false, updatable = false, insertable = false)
    private ClientInstitution clientInstitution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getHst() {
        return hst;
    }

    public void setHst(String hst) {
        this.hst = hst;
    }

    public String getNumberDoc() {
        return numberDoc;
    }

    public void setNumberDoc(String numberDoc) {
        this.numberDoc = numberDoc;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getReferPhone() {
        return referPhone;
    }

    public void setReferPhone(String referPhone) {
        this.referPhone = referPhone;
    }

    public String getTypeDoc() {
        return typeDoc;
    }

    public void setTypeDoc(String typeDoc) {
        this.typeDoc = typeDoc;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getSupevisor() {
        return supevisor;
    }

    public void setSupevisor(String supevisor) {
        this.supevisor = supevisor;
    }

/*    public List<CustomerOrder> getCustomerOrders() {
        return customerOrders;
    }

    public void setCustomerOrders(List<CustomerOrder> customerOrders) {
        this.customerOrders = customerOrders;
    }*/

    public ClientPerson getClientPerson() {
        return clientPerson;
    }

    public void setClientPerson(ClientPerson clientPerson) {
        this.clientPerson = clientPerson;
    }

    public ClientInstitution getClientInstitution() {
        return clientInstitution;
    }

    public void setClientInstitution(ClientInstitution clientInstitution) {
        this.clientInstitution = clientInstitution;
    }

    public String getFullName() {
        //return (lastName != null ? lastName + " " : "") + (maidenName != null ? maidenName + " " : "") + (firstName != null ? firstName : "");
        return (type.equals("P") ? clientPerson.getName() + " " + clientPerson.getLastName() + " " + clientPerson.getMaidenName()
                                 : clientInstitution.getName() );
    }
}
