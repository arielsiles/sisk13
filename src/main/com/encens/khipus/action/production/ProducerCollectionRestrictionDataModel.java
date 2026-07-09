package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProducerCollectionRestriction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("producerCollectionRestrictionDataModel")
@Scope(ScopeType.PAGE)
public class ProducerCollectionRestrictionDataModel extends QueryDataModel<Long, ProducerCollectionRestriction> {

    private static final String[] RESTRICTIONS = {
            "producerCollectionRestriction.state = #{producerCollectionRestrictionDataModel.criteria.state}"
    };

    @Override
    public String getEjbql() {
        return "select producerCollectionRestriction from ProducerCollectionRestriction producerCollectionRestriction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
