package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Destination;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Destination
 *
 * @author:
 */
@Name("destinationDataModel")
@Scope(ScopeType.PAGE)
public class DestinationDataModel extends QueryDataModel<Long, Destination> {

    private static final String[] RESTRICTIONS =
            {"lower(destination.name) like concat('%', concat(lower(#{destinationDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "destination.name";
    }

    @Override
    public String getEjbql() {
        return "select destination from Destination destination";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
