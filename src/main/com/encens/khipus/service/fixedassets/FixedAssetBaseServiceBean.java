package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetPart;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author
 * @version 3.1
 */

@Stateless
@Name("fixedAssetBaseService")
@AutoCreate
public class FixedAssetBaseServiceBean extends GenericServiceBean implements FixedAssetBaseService {
    public void updateFixedAssetSerialNumber(Long fixedAssetId, String serialNumber) {
        FixedAsset instance = getEntityManager().find(FixedAsset.class, fixedAssetId);
        if (null == instance) {
            return;
        }

        instance.setSequence(serialNumber);

        getEntityManager().merge(instance);
        getEntityManager().flush();
    }

    public void updateFixedAssetPartSerialNumber(Long fixedAssetPartId, String serialNumber) {
        FixedAssetPart part = getEntityManager().find(FixedAssetPart.class, fixedAssetPartId);

        if (null == part) {
            return;
        }

        part.setSerialNumber(serialNumber);
        getEntityManager().merge(part);
        getEntityManager().flush();
    }

    public FixedAsset getFixedAsset(Long id) {
        return getEntityManager().find(FixedAsset.class, id);
    }

    @SuppressWarnings(value = "unchecked")
    public List<FixedAssetPart> getFixedAssetParts(Long fixedAssetId) {
        FixedAsset instance = getEntityManager().find(FixedAsset.class, fixedAssetId);
        if (null == instance) {
            return null;
        }

        return (List<FixedAssetPart>) getEntityManager()
                .createNamedQuery("FixedAssetPart.findByFixedAsset")
                .setParameter("fixedAsset", instance)
                .getResultList();
    }
}
