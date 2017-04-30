package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "SubGroup.countByCode",
                query = "select count(sg.id.subGroupCode) " +
                        "from SubGroup sg " +
                        "where lower(sg.id.subGroupCode)=lower(:subGroupCode) " +
                        "and lower(sg.id.groupCode)=lower(:groupCode) " +
                        "and sg.id.companyNumber=:companyNumber")
})

@Entity
@Table(name = "inv_subgrupos", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class SubGroup implements BaseModel {
    @EmbeddedId
    private SubGroupPK id = new SubGroupPK();

    @Column(name = "COD_GRU", nullable = false, updatable = false, insertable = false)
    private String groupCode;

    @Column(name = "COD_SUB", nullable = false, updatable = false, insertable = false)
    private String subGroupCode;

    @Column(name = "DESCRI", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "ESTADO", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private SubGroupState state;

    @Version
    @Column(name = "version")
    private long version;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_GRU", nullable = false, insertable = false, updatable = false)
    })
    private Group group;

    public SubGroupPK getId() {
        return id;
    }

    public void setId(SubGroupPK id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubGroupState getState() {
        return state;
    }

    public void setState(SubGroupState state) {
        this.state = state;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public void setSubGroupCode(String subGroupCode) {
        this.subGroupCode = subGroupCode;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getFullName() {
        return getSubGroupCode() + " - " + getName();
    }

    @Override
    public String toString() {
        return "SubGroup{" +
                "id=" + id.getSubGroupCode() +
                ", groupCode='" + groupCode + '\'' +
                ", subGroupCode='" + subGroupCode + '\'' +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", version=" + version +
                ", group=" + group +
                '}';
    }
}
