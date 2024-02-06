package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.CollectMaterial;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface CollectMaterialService {
    List<CollectMaterial> findCollectMaterialNoAccounting(Date startDate, Date endDate);

    String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList,Date starDate,Date endDate);
}
