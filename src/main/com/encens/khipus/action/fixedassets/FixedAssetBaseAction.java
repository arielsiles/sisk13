package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.fixedassets.FixedAsset;
import com.encens.khipus.model.fixedassets.FixedAssetPart;
import com.encens.khipus.service.fixedassets.FixedAssetBaseService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.List;

/**
 * @author
 * @version 3.1
 */

@Name("fixedAssetBaseAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetBaseAction extends GenericAction<FixedAsset> {

    private FixedAsset fixedAsset;

    private List<FixedAssetPart> fixedAssetParts;

    @In
    private FixedAssetBaseService fixedAssetBaseService;

    public void updateFixedAssetSerialNumber(Long id, FixedAsset fixedAsset) {
        fixedAssetBaseService.updateFixedAssetSerialNumber(id, fixedAsset.getSequence());
    }

    public void updateFixedAssetPartSerialNumber(Long id, FixedAssetPart fixedAssetPart) {
        fixedAssetBaseService.updateFixedAssetPartSerialNumber(id, fixedAssetPart.getSerialNumber());
    }

    public void putFixedAssetId(Long id) {
        initialize(id);
    }

    private void initialize(Long id) {
        this.fixedAsset = fixedAssetBaseService.getFixedAsset(id);
        this.fixedAssetParts = fixedAssetBaseService.getFixedAssetParts(id);
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public List<FixedAssetPart> getFixedAssetParts() {
        return fixedAssetParts;
    }

    public void setFixedAssetParts(List<FixedAssetPart> fixedAssetParts) {
        this.fixedAssetParts = fixedAssetParts;
    }
}
