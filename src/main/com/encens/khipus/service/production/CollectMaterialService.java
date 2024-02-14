package com.encens.khipus.service.production;

import com.encens.khipus.model.production.CollectMaterial;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface CollectMaterialService {
    List<CollectMaterial> findCollectMaterialNoAccounting(Date startDate, Date endDate);

    List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate);
    List<CollectMaterial> findApprovedCollectMaterialByCode(String productItemCode, Date startDate, Date endDate);

    String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList,Date starDate,Date endDate);
}
