package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("socialWelfareEntityDataModel")
@Scope(ScopeType.PAGE)
public class SocialWelfareEntityDataModel extends QueryDataModel<Long, SocialWelfareEntity> {
    private static final String[] RESTRICTIONS = {
            "lower(socialWelfareEntity.name) like concat('%', concat(lower(#{socialWelfareEntityDataModel.criteria.name}), '%'))",
            "socialWelfareEntity.type = #{socialWelfareEntityDataModel.criteria.type}"
    };

    @Create
    public void init() {
        sortProperty = "socialWelfareEntity.name";
    }

    @Override
    public String getEjbql() {
        return "select socialWelfareEntity from SocialWelfareEntity socialWelfareEntity" +
                " left join fetch socialWelfareEntity.provider provider" +
                " left join fetch provider.entity entity";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
