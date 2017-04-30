package com.encens.khipus.action.budget;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.budget.ClassifierType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * This class is a data model for burden list
 *
 * @author
 * @version 2.0
 */
@Name("burdenDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CLASSIFIERS','VIEW')}")
public class BurdenDataModel extends QueryDataModel<Long, Classifier> {
    private ClassifierType classifierType = ClassifierType.BURDEN;
    private static final String[] RESTRICTIONS = {
            "classifier.type = #{burdenDataModel.classifierType}",
            "lower(classifier.name) like concat('%', concat(lower(#{burdenDataModel.criteria.name}), '%'))",
            "lower(classifier.code) like concat(lower(#{burdenDataModel.criteria.code}), '%')"};

    @Create
    public void init() {
        sortProperty = "classifier.name";
    }

    @Override
    public String getEjbql() {
        return "select classifier from Classifier classifier";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public ClassifierType getClassifierType() {
        return classifierType;
    }
}
