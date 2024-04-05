package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ZoneInspection;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("zoneInspectionDataModel")
@Scope(ScopeType.PAGE)
public class ZoneInspectionDataModel extends QueryDataModel<Long, ZoneInspection> {

    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS = {
            "zoneInspection.date >= #{zoneInspectionDataModel.startDate}",
            "zoneInspection.date <= #{zoneInspectionDataModel.endDate}"
    };

    @Create
    public void init() {
        sortProperty = "zoneInspection.date";
    }

    @Override
    public String getEjbql() {
        return "select zoneInspection from ZoneInspection zoneInspection";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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
}
