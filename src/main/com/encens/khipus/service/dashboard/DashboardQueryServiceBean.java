package com.encens.khipus.service.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.factory.DtoFactory;
import com.encens.khipus.dashboard.component.dto.factory.InstanceBuilder;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.factory.DashboardObject;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.component.totalizer.Totalizer;
import com.encens.khipus.framework.service.GenericServiceBean;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * @author
 * @version 2.6
 */

@Stateless
@Name("dashboardQueryService")
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class DashboardQueryServiceBean extends GenericServiceBean implements DashboardQueryService {

    @SuppressWarnings(value = "unchecked")
    public <T extends DashboardObject> List<T> executeQuery(String sql, ComponentFactory<T, SumTotalizer<T>> factory) {
        List queryResult = getEntityManager().createNativeQuery(sql).getResultList();
        return factory.getResultList(queryResult);
    }

    public <T extends DashboardObject> List<T> executeQuery(ComponentFactory<T, ? extends Totalizer<T>> factory) {
        List queryResult = getEntityManager().createNativeQuery(factory.getSqlQuery().getSql()).getResultList();
        return factory.getResultList(queryResult);
    }

    public List<Dto> getData(DtoConfiguration configuration, InstanceBuilder instanceBuilder, SqlQuery sqlQuery) {
        List queryResult = getEntityManager().createNativeQuery(sqlQuery.getSql()).getResultList();

        DtoFactory dtoFactory = new DtoFactory(configuration, instanceBuilder);
        return dtoFactory.getDtoList(queryResult);
    }
}
