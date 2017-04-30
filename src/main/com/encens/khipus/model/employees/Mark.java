package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mark
 *
 * @author
 * @version 1.2.4
 */
@NamedQueries(
        @NamedQuery(name = "Mark.findUnprocessedMarks",
                query = "select mark from Mark mark " +
                        "where mark.marDate>=:initRange " +
                        "and mark.marDate<=:endRange " +
                        "and mark not in (  select markState.mark from MarkState markState " +
                        "                   where markState.marDate>=:initRange " +
                        "                   and markState.marDate<=:endRange) " +
                        "order by mark.marRefCard, mark.marDate, mark.marTime ")
)
@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "VMARCADO")
public class Mark implements BaseModel {
    @Id
    @Column(name = "IDRHMARCADO", nullable = false, insertable = false, updatable = false)
    private Long id;

    @Column(name = "MARFECHA", insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date marDate;

    @Column(name = "MARIPPC", insertable = false, updatable = false)
    private String marIpPc;

    @Column(name = "MARPERID", insertable = false, updatable = false)
    private Integer marPerId;

    @Column(name = "MARREFTARJETA", insertable = false, updatable = false)
    private String marRefCard;

    @Column(name = "MARESTADO", insertable = false, updatable = false)
    private String marState;

    @Column(name = "SEDE", insertable = false, updatable = false)
    private String seat;

    @Column(name = "IDCOMPANIA", insertable = false, updatable = false)
    private Long companyId;

    /*@Column(name = "MAR_IN_OUT", insertable = false, updatable = false)
    private Number marInOut;*/

    @Column(name = "MARHORA", insertable = false, updatable = false)
    @Temporal(TemporalType.TIME)
    private Date marTime;

    @Transient
    private Date startMarDate = new Date();

    @Transient
    private Date endMarDate = new Date();

    @Transient
    private HoraryBandStateType horaryBandStateType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mark")
    private List<MarkState> markStateList = new ArrayList<MarkState>(0);

    public Mark() {

    }

    public Mark(Mark mark, HoraryBandStateType horaryBandStateType) {
        this.id = mark.getId();
        this.marDate = mark.getMarDate();
        this.marIpPc = mark.getMarIpPc();
        this.marPerId = mark.getMarPerId();
        this.marRefCard = mark.getMarRefCard();
        this.marState = mark.getMarState();
        this.seat = mark.getSeat();
        this.companyId = mark.getCompanyId();
        /*this.marInOut = mark.getMarInOut();*/
        this.marTime = mark.getMarTime();
        this.startMarDate = mark.getStartMarDate();
        this.endMarDate = mark.getEndMarDate();
        this.horaryBandStateType = horaryBandStateType;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getMarDate() {
        return marDate;
    }

    public void setMarDate(Date marDate) {
        this.marDate = marDate;
    }

    public String getMarIpPc() {
        return marIpPc;
    }

    public void setMarIpPc(String marIpPc) {
        this.marIpPc = marIpPc;
    }

    public Integer getMarPerId() {
        return marPerId;
    }

    public void setMarPerId(Integer marPerId) {
        this.marPerId = marPerId;
    }

    public String getMarRefCard() {
        return marRefCard;
    }

    public void setMarRefCard(String marRefCard) {
        this.marRefCard = marRefCard;
    }

    public String getMarState() {
        return marState;
    }

    public void setMarState(String marState) {
        this.marState = marState;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    /*public String getMarInOut() {
        return marInOut;
    }

    public void setMarInOut(String marInOut) {
        this.marInOut = marInOut;
    }*/

    public Date getMarTime() {
        return marTime;
    }

    public void setMarTime(Date marTime) {
        this.marTime = marTime;
    }

    public Date getStartMarDate() {
        return startMarDate;
    }

    public void setStartMarDate(Date startMarDate) {
        this.startMarDate = startMarDate;
    }

    public Date getEndMarDate() {
        return endMarDate;
    }

    public void setEndMarDate(Date endMarDate) {
        this.endMarDate = endMarDate;
    }

    public HoraryBandStateType getHoraryBandStateType() {
        return horaryBandStateType;
    }

    public void setHoraryBandStateType(HoraryBandStateType horaryBandStateType) {
        this.horaryBandStateType = horaryBandStateType;
    }

    public List<MarkState> getMarkStateList() {
        return markStateList;
    }

    public void setMarkStateList(List<MarkState> markStateList) {
        this.markStateList = markStateList;
    }
}
