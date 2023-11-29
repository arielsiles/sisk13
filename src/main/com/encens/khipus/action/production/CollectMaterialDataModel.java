package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.CollectMaterial;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("collectMaterialDataModel")
@Scope(ScopeType.PAGE)
public class CollectMaterialDataModel extends QueryDataModel<Long, CollectMaterial> {

    private String name;

    private static final String[] RESTRICTIONS = {
            "collectMaterial.date = #{collectMaterialDataModel.criteria.date}",
            "upper(collectMaterial.code) like concat(concat('%',upper(#{collectMaterialDataModel.criteria.code})), '%')",
            "upper(collectMaterial.producer.firstName) like concat(concat('%',upper(#{collectMaterialDataModel.name})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "collectMaterial.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select collectMaterial from CollectMaterial collectMaterial";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
