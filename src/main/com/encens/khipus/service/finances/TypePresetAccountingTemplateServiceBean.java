package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.CustomerCategoryType;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.finances.TypePresetAccountingTemplate;
import com.encens.khipus.service.customers.CustomerCategoryService;
import com.encens.khipus.service.customers.PriceItemService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@Name("typePresetAccountingTemplateService")
@AutoCreate
public class TypePresetAccountingTemplateServiceBean extends GenericServiceBean implements TypePresetAccountingTemplateService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private CustomerCategoryService customerCategoryService;

    @Override
    public List<TypePresetAccountingTemplate> getTypePresetAccountingTemplates(PresetAccountingTemplate presetAccountingTemplate) {

        List<TypePresetAccountingTemplate> resultList = em.createQuery("select p from TypePresetAccountingTemplate p " +
                " where p.presetAccountingTemplate =:presetAccountingTemplate")
                .setParameter("presetAccountingTemplate", presetAccountingTemplate)
                .getResultList();

        return resultList;
    }
    @Override
    public void updateTypePresetAccountingTemplates(List<TypePresetAccountingTemplate> items) {

        for (TypePresetAccountingTemplate item : items){

            if (em.contains(item)){

                em.merge(item);
            }else{

                em.persist(item);
            }
            em.flush();
        }
    }

    @Override
    public Map<String, BigDecimal> getTypePresetAccountingTemplateMap(PresetAccountingTemplate presetAccountingTemplate) {
        return null;
    }

    @Override
    public Map<String, BigDecimal> getTypePresetAccountingTemplates() {
        return null;
    }


    @Override
    public void deleteTypePresetAccountingTemplate(TypePresetAccountingTemplate typePresetAccountingTemplate) {

    }


}
