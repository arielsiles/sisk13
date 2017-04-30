package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.model.warehouse.SubGroupPK;
import com.encens.khipus.model.warehouse.SubGroupState;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author
 * @version 2.1
 */
@Stateless
@Name("warehouseCatalogService")
@AutoCreate
public class WarehouseCatalogServiceBean extends GenericServiceBean implements WarehouseCatalogService {

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public boolean isSubgroupValid(SubGroupPK id) {
        SubGroup subGroup = listEm.find(SubGroup.class, id);
        return null != subGroup && SubGroupState.VIG.equals(subGroup.getState());
    }

    public boolean isValidState(Object entity, Enum<? extends Enum> constant) {
        Class clazz = entity.getClass();
        if (!(entity instanceof BaseModel)) {
            throw new IllegalArgumentException("The " + clazz.getName()
                    + " should be implements the " + BaseModel.class.getName() + " interface");
        }


        Object dbObject = listEm.find(entity.getClass(), ((BaseModel) entity).getId());
        if (null == dbObject) {
            return true;
        }

        Object enumValue = getEnumValue(dbObject, constant);

        return constant.equals(enumValue);
    }

    public <T> boolean isValidState(Class<T> clazz, Object id, Enum<? extends Enum> constant) {

        Object object = findCatalogById(clazz, id);
        if (null == object) {
            return false;
        }

        Class constantClass = constant.getClass();

        Class instanceClazz = object.getClass();

        Object enumValue = null;
        for (; instanceClazz != Object.class; instanceClazz = instanceClazz.getSuperclass()) {
            for (Field field : instanceClazz.getDeclaredFields()) {
                if (constantClass.equals(field.getType())) {
                    try {
                        field.setAccessible(true);
                        enumValue = field.get(object);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("It isn't possible to get the field with type " + constantClass.getName());
                    }
                }
            }
        }

        if (null == enumValue) {
            throw new IllegalArgumentException("It isn't possible to get the field with type " + constantClass.getName());
        }

        return constant.equals(enumValue);
    }

    public <T> boolean existWarehouseCatalogInDataBase(Class<T> clazz, Object id) {
        Object object = findCatalogById(clazz, id);
        return null != object;
    }


    public <T> T findWarehouseCatalog(Class<T> clazz, Object id) {
        if (existWarehouseCatalogInDataBase(clazz, id)) {
            T element = getEntityManager().find(clazz, id);
            getEntityManager().refresh(element);
            return element;
        }

        return null;
    }

    public boolean isInUse(List<String> queries) {
        for (String query : queries) {
            log.debug("Executing :" + query);
            List result = getEntityManager().createQuery(query).getResultList();
            if (null != result && !result.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private <T> T findCatalogById(Class<T> clazz, Object id) {
        T object = listEm.find(clazz, id);
        if (null == object) {
            return null;
        }

        return object;
    }

    private Object getEnumValue(Object instance, Enum<? extends Enum> constant) {
        Class constantClazz = constant.getClass();
        Class instanceClazz = instance.getClass();

        Object enumValue = null;
        for (; instanceClazz != Object.class; instanceClazz = instanceClazz.getSuperclass()) {
            for (Field field : instanceClazz.getDeclaredFields()) {
                if (constantClazz.equals(field.getType())) {
                    try {
                        field.setAccessible(true);
                        enumValue = field.get(instance);
                        break;
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("It isn't possible to get the field with type " + constantClazz.getName());
                    }
                }
            }
        }

        return enumValue;
    }

    public Boolean validateProductItemCode(String productItemCode) {
        if (!ValidatorUtil.isBlankOrNull(productItemCode)) {
            Long countResult = ((Long) listEm.createNamedQuery("ProductItem.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("productItemCode", productItemCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }

    public Boolean validateGroupCode(String groupCode) {
        if (!ValidatorUtil.isBlankOrNull(groupCode)) {
            Long countResult = ((Long) listEm.createNamedQuery("Group.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("groupCode", groupCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }

    public Boolean validateSubGroupCode(String groupCode, String subGroupCode) {
        if (!ValidatorUtil.isBlankOrNull(groupCode) && !ValidatorUtil.isBlankOrNull(subGroupCode)) {
            Long countResult = ((Long) listEm.createNamedQuery("SubGroup.countByCode")
                    .setParameter("companyNumber", Constants.defaultCompanyNumber)
                    .setParameter("groupCode", groupCode)
                    .setParameter("subGroupCode", subGroupCode).getSingleResult());
            return countResult == null || countResult == 0;
        }
        return false;
    }
}
