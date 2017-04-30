package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Zone;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Zone
 *
 * @author:
 */

@Name("zoneDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ZONE','VIEW')}")
public class ZoneDataModel extends QueryDataModel<Long, Zone> {

    private static final String[] RESTRICTIONS =
            {"lower(zone.name) like concat('%', concat(lower(#{zoneDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "zone.name";
    }

    @Override
    public String getEjbql() {
        return "select zone from Zone zone";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
