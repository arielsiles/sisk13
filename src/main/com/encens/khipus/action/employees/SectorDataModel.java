package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Sector;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Sector
 *
 * @author
 */

@Name("sectorDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SECTOR','VIEW')}")
public class SectorDataModel extends QueryDataModel<Long, Sector> {
    private static final String[] RESTRICTIONS =
            {"lower(sector.name) like concat('%', concat(lower(#{sectorDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "sector.name";
    }

    @Override
    public String getEjbql() {
        return "select sector from Sector sector";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}