package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.CustomerCategoryType;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.finances.TypePresetAccountingTemplate;

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
public interface TypePresetAccountingTemplateService extends GenericService {

    List<TypePresetAccountingTemplate> getTypePresetAccountingTemplates(PresetAccountingTemplate presetAccountingTemplate);
    Map<String, BigDecimal> getTypePresetAccountingTemplateMap(PresetAccountingTemplate presetAccountingTemplate);
    Map<String, BigDecimal> getTypePresetAccountingTemplates();
    void updateTypePresetAccountingTemplates(List<TypePresetAccountingTemplate> typePresetAccountingTemplates);
    void deleteTypePresetAccountingTemplate(TypePresetAccountingTemplate typePresetAccountingTemplate);
}