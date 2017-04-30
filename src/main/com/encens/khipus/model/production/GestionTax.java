package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TableGenerator(name = "GestionTax_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "gestionimpuesto",
        allocationSize = 10)

@Entity
@Table(name = "gestionimpuesto")
public class GestionTax implements BaseModel {

    @Id
    @Column(name = "idgestionimpuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GestionTax_Generator")
    private Long id;

    @Column(name = "fechainicio")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @OneToMany(mappedBy = "gestionTax", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<ProducerTax> producerTaxes = new ArrayList<ProducerTax>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<ProducerTax> getProducerTaxes() {
        return producerTaxes;
    }

    public void setProducerTaxes(List<ProducerTax> producerTaxes) {
        this.producerTaxes = producerTaxes;
    }
}
