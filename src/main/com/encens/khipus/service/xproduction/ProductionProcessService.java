package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.xproduction.ProductionProcess;
import com.encens.khipus.model.xproduction.XMachineProcess;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PriceItemService service interface
 *
 * @author
 * @version 2.7
 */

@Local
public interface ProductionProcessService extends GenericService {

    List<XMachineProcess> getXMachineProcess(ProductionProcess productionProcess);
    Map<String, BigDecimal> getTypePresetAccountingTemplateMap(PresetAccountingTemplate presetAccountingTemplate);
    Map<String, BigDecimal> getTypePresetAccountingTemplates();
    void updateXMachineProcesses(List<XMachineProcess> xMachineProcess);
    void deleteXMachineProcess(XMachineProcess xMachineProcess);
}