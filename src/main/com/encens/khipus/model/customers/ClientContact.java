package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.contacts.City;
import com.encens.khipus.model.contacts.Country;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Persona de contacto de un Cliente (modelo local de ventas, estilo Odoo).
 * <p/>
 * Es una tabla de RELACION: cuelga de {@link Client} (parent) y guarda los datos
 * del contacto y su vinculo (cargo, si es principal, activo). NO forma parte de la
 * jerarquia global Entity/Person para no alterar ventas, pero deja una costura
 * opcional ({@link #person}) para reconciliar con el modelo unificado a futuro.
 *
 * @author
 * @version 1.0
 */
@NamedQueries(
        {
                @NamedQuery(name = "ClientContact.findByClient",
                        query = "select cc from ClientContact cc where cc.client =:client order by cc.primaryContact desc, cc.firstName"),
                @NamedQuery(name = "ClientContact.findActiveByClient",
                        query = "select cc from ClientContact cc where cc.client =:client and cc.active =:active order by cc.primaryContact desc, cc.firstName")
        }
)

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ClientContact.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "contactocliente",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = Constants.KHIPUS_SCHEMA, name = "contactocliente")
public class ClientContact implements BaseModel {

    @Id
    @Column(name = "idcontactocliente", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ClientContact.tableGenerator")
    private Long id;

    /* ---- vinculo (parent) ---- */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idpersonacliente", nullable = false)
    private Client client;

    /* ---- costura opcional hacia el modelo unificado (NULL por defecto) ---- */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpersona", nullable = true)
    private Person person;

    /* ---- identidad del contacto ---- */

    @Column(name = "nombres", nullable = false, length = 200)
    @NotNull
    @Length(max = 200)
    private String firstName;

    @Column(name = "apellidos", length = 200)
    @Length(max = 200)
    private String lastName;

    @Column(name = "cargo", length = 150)
    @Length(max = 150)
    private String position;

    @Column(name = "empresa", length = 200)
    @Length(max = 200)
    private String companyName;

    @Column(name = "area", length = 150)
    @Length(max = 150)
    private String area;

    /* ---- canales de contacto (nivel empresarial) ---- */

    @Column(name = "email", length = 150)
    @Length(max = 150)
    private String email;

    @Column(name = "telefono", length = 30)
    @Length(max = 30)
    private String phone;

    @Column(name = "celular", length = 30)
    @Length(max = 30)
    private String mobile;

    @Column(name = "telefonotrabajo", length = 30)
    @Length(max = 30)
    private String workPhone;

    @Column(name = "web", length = 200)
    @Length(max = 200)
    private String website;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpais", nullable = true)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddepartamento", nullable = true)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idciudad", nullable = true)
    private City city;

    @Column(name = "direccion", length = 300)
    @Length(max = 300)
    private String address;

    @Column(name = "observaciones", length = 1000)
    @Length(max = 1000)
    private String notes;

    /* ---- estado del vinculo ---- */

    @Column(name = "principal", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean primaryContact = Boolean.FALSE;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active = Boolean.TRUE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(Boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getFullName() {
        return (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
    }

    public String getFullNameAndPosition() {
        String fullName = getFullName().trim();
        return StringUtils.isEmpty(position) ? fullName : fullName + " (" + position + ")";
    }
}
