package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.XFormulation;
import com.encens.khipus.model.xproduction.XFormulationInput;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id: ProductServiceBean.java 2008-9-11 13:50:57 $
 */
@Stateless
    @Name("xformulationService")
@AutoCreate
public class XFormulationServiceBean implements XFormulationService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    public void updateFormulation(XFormulation formulation, List<XFormulationInput> formulationInputList){

        for (XFormulationInput formulationInput : formulationInputList){

            if (formulationInput.getId() == null){
                formulationInput.setFormulation(formulation);
                em.persist(formulationInput);
                em.flush();
            }else {
                em.merge(formulationInput);
                em.flush();
            }
        }
        em.merge(formulation);
        em.flush();
    }

    public void removeInput(XFormulationInput formulationInput){
        if (em.contains(formulationInput)){
            em.remove(formulationInput);
            em.flush();
        }
    }
}
