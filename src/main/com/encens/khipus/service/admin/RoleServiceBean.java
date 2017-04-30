package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.Role;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Role service
 *
 * @author
 * @version 1.0
 */
@Stateless
@Name("roleService")
@AutoCreate
public class RoleServiceBean implements RoleService {
    @In
    private EntityManager entityManager;

    @SuppressWarnings({"unchecked"})
    public List<Role> getList(int firstRow, int maxResults, String sortProperty, boolean sortAsc, Role roleCriteria) {
        StringBuilder queryString = new StringBuilder("select role from Role role");
        if (roleCriteria.getName() != null) {
            queryString.append(" where lower(role.name) like concat('%', concat(lower(:name), '%'))");
        }
        queryString.append(" order by ").append(sortProperty).append(sortAsc ? " ASC" : " DESC");

        Query query = entityManager.createQuery(queryString.toString());
        query.setFirstResult(firstRow);
        query.setMaxResults(maxResults);
        if (roleCriteria.getName() != null) {
            query.setParameter("name", roleCriteria.getName());
        }
        return query.getResultList();
    }

    public Long getCount(String sortProperty, boolean sortAsc, Role roleCriteria) {
        StringBuilder queryString = new StringBuilder("select count(*) from Role role");
        if (roleCriteria.getName() != null) {
            queryString.append(" where lower(role.name) like concat('%', concat(lower(:name), '%'))");
        }
        queryString.append(" order by ").append(sortProperty).append(sortAsc ? " ASC" : " DESC");
        Query query = entityManager.createQuery(queryString.toString());
        if (roleCriteria.getName() != null) {
            query.setParameter("name", roleCriteria.getName());
        }
        return (Long) query.getSingleResult();
    }
}
