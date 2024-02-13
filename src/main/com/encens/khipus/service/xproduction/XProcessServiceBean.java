package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.finances.TypePresetAccountingTemplate;
import com.encens.khipus.model.xproduction.XMachineProcess;
import com.encens.khipus.model.xproduction.XProcess;
import com.encens.khipus.service.customers.CustomerCategoryService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Stateless
@Name("xProcessService")
@AutoCreate
public class XProcessServiceBean extends GenericServiceBean implements XProcessService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private CustomerCategoryService customerCategoryService;

    @Override
    public List<XMachineProcess> getXMachineProcess(XProcess xProcess) {

        List<XMachineProcess> resultList = em.createQuery("select p from XMachineProcess p " +
                " where p.xProcess =:xProcess")
                .setParameter("xProcess", xProcess)
                .getResultList();

        return resultList;
    }
    @Override
    public void updateXMachineProcesses(List<XMachineProcess> items) {

        for (XMachineProcess item : items){

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
    public void deleteXMachineProcess(XMachineProcess xMachineProcess) {

    }


}
