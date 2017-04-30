package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Name("rotatoryFundCollectionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROTATORYFUNDCOLLECTION','VIEW')}")
public class RotatoryFundCollectionDataModel extends QueryDataModel<Long, RotatoryFundCollection> {
    private static final String[] RESTRICTIONS = {
            "rotatoryFund.id = #{rotatoryFund.id}"
    };

    @Create
    public void init() {
        sortProperty = "rotatoryFundCollection.creationDate, rotatoryFundCollection.code";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select rotatoryFundCollection from RotatoryFundCollection rotatoryFundCollection" +
                " left join fetch rotatoryFundCollection.collectionDocument collectionDocument" +
                " left join fetch rotatoryFundCollection.bankAccount bankAccount" +
                " left join rotatoryFundCollection.rotatoryFund rotatoryFund" +
                " where true=#{not empty rotatoryFund.id}";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}