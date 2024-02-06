package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.MetaProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("collectMaterialByProducerDataModel")
@Scope(ScopeType.PAGE)
public class CollectMaterialByProducerDataModel extends QueryDataModel<Long, CollectMaterial> {

    private String name;
    private String lastName;
    private Date startDate;
    private Date endDate;
    private MetaProduct metaProduct;

    private static final String[] RESTRICTIONS = {
            "collectMaterial.producer = #{rawMaterialPaymentAction.instance.producer}",
            "upper(collectMaterial.code) like concat(concat('%',upper(#{collectMaterialByProducerDataModel.criteria.code})), '%')",
            "upper(collectMaterial.producer.firstName) like concat(concat('%',upper(#{collectMaterialByProducerDataModel.name})), '%')",
            "upper(collectMaterial.producer.lastName) like concat(concat('%',upper(#{collectMaterialByProducerDataModel.lastName})), '%')",
            "collectMaterial.metaProduct = #{collectMaterialByProducerDataModel.metaProduct}",
            "collectMaterial.date >= #{collectMaterialByProducerDataModel.startDate}",
            "collectMaterial.date <= #{collectMaterialByProducerDataModel.endDate}",
            "collectMaterial.state = #{rawMaterialPaymentAction.approvedStatus}"
    };

    @Create
    public void init() {
        sortProperty = "collectMaterial.date, collectMaterial.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select collectMaterial " +
                "from CollectMaterial collectMaterial ";
                // + "left join fetch collectMaterial.producer ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public List<CollectMaterial> getSelectedItems() {
        List ids = super.getSelectedIdList();

        List<CollectMaterial> result = new ArrayList<CollectMaterial>();
        for (Object id : ids) {
            CollectMaterial item = getEntityManager().find(CollectMaterial.class, id);
            result.add(item);
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }
}
