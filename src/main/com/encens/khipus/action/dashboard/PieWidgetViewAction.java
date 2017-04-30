package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Widget;
import org.jboss.seam.annotations.Name;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2
 */
@Name("pieWidgetViewAction")
public class PieWidgetViewAction<T extends PieGraphic> extends WidgetViewAction<T> {
    private Map<String, List<Dto>> resultMap = new LinkedHashMap<String, List<Dto>>();

    protected void initialize() {
        T instance = null;
        try {
            //noinspection unchecked
            instance = (T) ((Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        setGraphic(instance);
        initializeWidget();
        resetFilters();
    }

    protected void resetFilters() {
        refresh();
    }

    protected void refresh() {

    }

    @Override
    protected T getGraphic() {
        search();
        graphic.setData(resultMap);
        graphic.setWidget(getWidget());
        graphic.setWidth(calculateGraphicWidth());
        setGraphicParameters(graphic);
        return graphic;
    }

    @Override
    protected void executeService(Widget widget, SqlQuery sqlQuery) {
        for (Filter filter : widget.getFilters()) {
            applyConfigurationFilter(filter, sqlQuery);

            List<Dto> result = dashboardQueryService.getData(getDtoConfiguration(), getInstanceBuilder(), sqlQuery);
            resultMap.put(filter.getName(), result);
        }
    }

    protected void applyConfigurationFilter(Filter filter, SqlQuery sqlQuery) {
        throw new UnsupportedOperationException("This method should be overwrite in the children classes");
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        return DtoConfiguration.getInstance(IdField.getInstance("value", 0));
    }

    protected Map<String, List<Dto>> getResultMap() {
        return resultMap;
    }
}
