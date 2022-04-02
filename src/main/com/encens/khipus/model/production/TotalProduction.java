package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "producciontotal")
public class TotalProduction implements BaseModel {

    @Id
    @Column(name = "fecha")
    private Date date;

    @Column(name = "cod_art")
    private String articleCode;

    @Column(name = "nombre")
    private String name;

    @Column(name = "cantidad_sc", columnDefinition = "DECIMAL(24,0)")
    private Double amountSC;

    @Column(name = "cantidad_sp", columnDefinition = "DECIMAL(24,0)")
    private Double amountSP;

    @Column(name = "reproceso_sc", columnDefinition = "DECIMAL(24,0)")
    private Double reprocessSC;

    @Column(name = "reproceso_sp", columnDefinition = "DECIMAL(24,0)")
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
