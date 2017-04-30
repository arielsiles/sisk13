package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "PRODUCCIONTOTAL")
public class TotalProduction implements BaseModel {

    @Id
    @Column(name = "FECHA")
    private Date date;

    @Column(name = "COD_ART")
    private String articleCode;

    @Column(name = "NOMBRE")
    private String name;

    @Column(name = "CANTIDAD_SC", columnDefinition = "DECIMAL(24,0)")
    private Double amountSC;

    @Column(name = "CANTIDAD_SP", columnDefinition = "DECIMAL(24,0)")
    private Double amountSP;

    @Column(name = "REPROCESO_SC", columnDefinition = "DECIMAL(24,0)")
    private Double reprocessSC;

    @Column(name = "REPROCESO_SP", columnDefinition = "DECIMAL(24,0)")
    private Double reprocessSP;


    public Long getId() {
        return null;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getAmountSC() {
        return amountSC;
    }

    public void setAmountSC(Double amountSC) {
        this.amountSC = amountSC;
    }

    public Double getAmountSP() {
        return amountSP;
    }

    public void setAmountSP(Double amountSP) {
        this.amountSP = amountSP;
    }

    public Double getReprocessSC() {
        return reprocessSC;
    }

    public void setReprocessSC(Double reprocessSC) {
        this.reprocessSC = reprocessSC;
    }

    public Double getReprocessSP() {
        return reprocessSP;
    }

    public void setReprocessSP(Double reprocessSP) {
        this.reprocessSP = reprocessSP;
    }
}
