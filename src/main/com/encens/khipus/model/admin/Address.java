package com.encens.khipus.model.admin;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Class to represent the address information
 *
 * @author
 * @version 1.0
 */
@Embeddable
public class Address implements Serializable {

    private static final long serialVersionUID = 5178693011799983391L;

    @Length(max = 80)
    @Column(name = "country", length = 80)
    private String country;

    @Length(max = 80)
    @Column(name = "city", length = 80)
    private String city;

    @Length(max = 80)
    @Column(name = "province", length = 80)
    private String province;

    @Length(max = 150)
    @Column(name = "street", length = 150)
    private String street;


    public Address() {
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
