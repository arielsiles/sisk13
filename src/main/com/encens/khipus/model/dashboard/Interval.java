package com.encens.khipus.model.dashboard;

import com.encens.khipus.model.admin.Company;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Represents a filter of type interval for a widget
 *
 * @author
 * @version 2.26
 */
@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "intervalocomppnl")
@DiscriminatorValue("intervalo")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idintervalocomppnl", referencedColumnName = "idfiltrocomppnl")
})
public class Interval extends Filter {

    @Column(name = "valormin", nullable = false)
    @NotNull
    private Integer minValue;

    @Column(name = "valormax", nullable = false)
    @NotNull
    private Integer maxValue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
