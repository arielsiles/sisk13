package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.IndirectCostsConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Diego on 08/07/2014.
 */
@Name("indirectCostsConfigDataModel")
@Scope(ScopeType.PAGE)
public class IndirectCostsConfigDataModel extends QueryDataModel<Long,IndirectCostsConfig> {

    private static final String[] RESTRICTIONS = {
             "upper(indirectCostsConfig.description) like concat(concat('%',upper(#{indirectCostsConfigDataModel.criteria.description})), '%')"
            ,"indirectCostsConfig.account = #{indirectCostsConfigDataModel.criteria.account}"
    };

    @Override
    public String getEjbql(){
        return "select indirectCostsConfig from IndirectCostsConfig indirectCostsConfig";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
