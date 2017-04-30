package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.fixedassets.FixedAssetMovementType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Service implementation of FixedAssetMovementTypeService
 *
 * @author
 * @version 2.25
 */

@Stateless
@Name("fixedAssetMovementTypeService")
@AutoCreate
public class FixedAssetMovementTypeServiceBean extends GenericServiceBean implements FixedAssetMovementTypeService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;
    @In(required = false)
    private User currentUser;


    public FixedAssetMovementType findInDataBase(Long id) {
        FixedAssetMovementType fixedAssetMovementType = listEm.find(FixedAssetMovementType.class, id);
        if (null == fixedAssetMovementType) {
            throw new RuntimeException("Cannot find the PurchaseOrder entity for id=" + id);
        }
        return fixedAssetMovementType;
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetMovementType> findAll() {
        return getEntityManager().createNamedQuery("FixedAssetMovementType.findAll").getResultList();
    }

}