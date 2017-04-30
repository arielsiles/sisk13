package com.encens.khipus.service.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.factory.InstanceBuilder;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.component.totalizer.Totalizer;
import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.6
 */
@Local
public interface DashboardQueryService extends GenericService {
    <T extends DashboardObject> List<T> executeQuery(String sql, ComponentFactory<T, SumTotalizer<T>> factory);

    <T extends DashboardObject> List<T> executeQuery(ComponentFactory<T, ? extends Totalizer<T>> factory);

    List<Dto> getData(DtoConfiguration configuration, InstanceBuilder instanceBuilder, SqlQuery sqlQuery);
}
