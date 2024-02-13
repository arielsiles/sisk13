package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.xproduction.XMachineProcess;
import com.encens.khipus.model.xproduction.XProcess;

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
public interface XProcessService extends GenericService {

    List<XMachineProcess> getXMachineProcess(XProcess xProcess);
    Map<String, BigDecimal> getTypePresetAccountingTemplateMap(PresetAccountingTemplate presetAccountingTemplate);
    Map<String, BigDecimal> getTypePresetAccountingTemplates();
    void updateXMachineProcesses(List<XMachineProcess> xMachineProcess);
    void deleteXMachineProcess(XMachineProcess xMachineProcess);
}