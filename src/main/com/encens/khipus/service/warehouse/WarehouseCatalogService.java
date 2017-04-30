package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.1
 */
@Local
public interface WarehouseCatalogService extends GenericService {
    boolean isValidState(Object entity, Enum<? extends Enum> constant);

    <T> boolean isValidState(Class<T> clazz, Object id, Enum<? extends Enum> constant);

    <T> boolean existWarehouseCatalogInDataBase(Class<T> clazz, Object id);

    <T> T findWarehouseCatalog(Class<T> clazz, Object id);

    boolean isInUse(List<String> queries);

    Boolean validateProductItemCode(String productItemCode);

    Boolean validateGroupCode(String groupCode);

    Boolean validateSubGroupCode(String groupCode, String subGroupCode);
}
