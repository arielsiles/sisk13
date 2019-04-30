package com.encens.khipus.service.production;

import com.encens.khipus.model.production.Formulation;
import com.encens.khipus.model.production.FormulationInput;
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
@Name("formulationService")
@AutoCreate
public class FormulationServiceBean implements FormulationService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    public void updateFormulation(Formulation formulation, List<FormulationInput> formulationInputList){

        for (FormulationInput formulationInput : formulationInputList){

            if (formulationInput.getId() == null){
                formulationInput.setFormulation(formulation);
                em.persist(formulationInput);
                em.flush();
            }else {
                em.merge(formulationInput);
                em.flush();
            }

        }
    }
}
