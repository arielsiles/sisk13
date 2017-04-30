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
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "INTERVALOCOMPPNL")
@DiscriminatorValue("intervalo")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "IDINTERVALOCOMPPNL", referencedColumnName = "IDFILTROCOMPPNL")
})
public class Interval extends Filter {

    @Column(name = "VALORMIN", nullable = false)
    @NotNull
    private Integer minValue;

    @Column(name = "VALORMAX", nullable = false)
    @NotNull
    private Integer maxValue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
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
