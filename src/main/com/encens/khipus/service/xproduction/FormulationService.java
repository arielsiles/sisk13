package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.Formulation;
import com.encens.khipus.model.xproduction.FormulationInput;

import javax.ejb.Local;
import java.util.List;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface FormulationService {

    void updateFormulation(Formulation formulation, List<FormulationInput> formulationInputList);
    void removeInput(FormulationInput formulationInput);

}
