package com.encens.khipus.action.dashboard;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import com.encens.khipus.model.dashboard.Responsible;
import com.encens.khipus.model.dashboard.Widget;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.dashboard.WidgetService;
import com.encens.khipus.util.MapUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.*;

/**
 * Widget action class
 *
 * @author
 * @version 2.26
 */
@Name("widgetAction")
@Scope(ScopeType.CONVERSATION)
public class WidgetAction extends GenericAction<Widget> {

    @In
    private WidgetService widgetService;

    @In
    private BusinessUnitService businessUnitService;

    @In(create = true)
    private Widget widget;

    private Long selectedBusinessUnitId;
    private Map<Long, Responsible> regionalResponsibles;
    private JobContract nationalResponsible;

    @Factory(value = "widget", scope = ScopeType.STATELESS)
    public Widget initWidget() {
        return getInstance();
    }

    @Override
    protected GenericService getService() {
        return widgetService;
    }

    @Begin(flushMode = FlushModeType.MANUAL)
    public void selectWidget(String xmlId) {
        Widget widget = widgetService.findByXmlId(xmlId);
        if (widget == null) {
            setInstance(widgetService.loadWidget(xmlId));
            setOp(OP_CREATE);
        } else {
            setInstance(widget);
            setOp(OP_UPDATE);
        }
        nationalResponsible = getInstance().getNationalResponsible();
    }

    @End(beforeRedirect = true)
    public String saveOrUpdate() {
        getInstance().setNationalResponsible(nationalResponsible);
        getInstance().getRegionalResponsibles().clear();
        getInstance().getRegionalResponsibles().addAll(regionalResponsibles.values());
        for (Iterator<Responsible> it = getInstance().getRegionalResponsibles().iterator(); it.hasNext();) {
            Responsible responsible = it.next();
            if (responsible.getBusinessUnit() == null || responsible.getResponsible() == null) {
                it.remove();
            }
        }

        if (isManaged()) {
            return update();
        } else {
            return create();
        }
    }

    public List<Interval> getIntervals() {
        List<Interval> intervals = new ArrayList<Interval>();

        for (Filter filter : widget.getFilters()) {
            if (filter instanceof Interval) {
                intervals.add((Interval) filter);
            }
        }

        return intervals;
    }

    public void selectBusinessUnitId(Long selectedBusinessUnitId) {
        this.selectedBusinessUnitId = selectedBusinessUnitId;
    }

    public void clearSelectedBusinessUnitId() {
        selectedBusinessUnitId = null;
    }

    public JobContract getNationalResponsible() {
        return nationalResponsible;
    }

    public void setNationalResponsible(JobContract nationalResponsible) {
    }

    public void assignResponsible(JobContract jobContract) {
        if (selectedBusinessUnitId == null) {
            nationalResponsible = jobContract;
        } else {
            Responsible responsible = regionalResponsibles.get(selectedBusinessUnitId);
            responsible.setResponsible(jobContract);
            responsible.setBusinessUnit(businessUnitService.findById(selectedBusinessUnitId));
        }
    }

    public void clearNationalResponsible() {
        nationalResponsible = null;
    }

    public Map<Long, Responsible> getRegionalResponsibles() {
        if (regionalResponsibles == null) {
            regionalResponsibles = new HashMap<Long, Responsible>() {
                @Override
                public Responsible get(Object key) {
                    if (key != null) {
                        return MapUtil.getNotNullValue(this, (Long) key, super.get(key));
                    }
                    return null;
                }
            };

            for (Responsible responsible : getInstance().getRegionalResponsibles()) {
                regionalResponsibles.put(responsible.getBusinessUnit().getId(), responsible);
            }
        }
        return regionalResponsibles;
    }

    public void clearRegionalResponsible(Long businessUnitId) {
        regionalResponsibles.remove(businessUnitId);
    }
}
