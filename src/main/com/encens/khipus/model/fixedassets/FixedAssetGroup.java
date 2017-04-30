package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for FixedAssetGroup
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "FixedAssetGroup.countByCode",
                query = "select count(g.groupCode) " +
                        "from FixedAssetGroup g " +
                        "where lower(g.groupCode)=lower(:groupCode) " +
                        "and g.id.companyNumber=:companyNumber")
})

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_grupos", schema = Constants.FINANCES_SCHEMA)
public class FixedAssetGroup implements BaseModel {

    @EmbeddedId
    private FixedAssetGroupPk id = new FixedAssetGroupPk();

    @Column(name = "grupo", nullable = false, updatable = false, insertable = false)
    private String groupCode;

    @Column(name = "descri", nullable = false, length = 100)
    @Length(max = 100)
    @NotNull
    private String description;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FixedAssetGroupPk getId() {
        return id;
    }

    public void setId(FixedAssetGroupPk id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getFullName() {
        return getId().getGroupCode() + " - " + getDescription();
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
