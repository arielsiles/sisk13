package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.Formulation;
import com.encens.khipus.model.production.FormulationInput;
import com.encens.khipus.model.xproduction.XFormulation;
import com.encens.khipus.model.xproduction.XFormulationInput;

import javax.ejb.Local;
import java.util.List;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface XFormulationService {

    void updateFormulation(XFormulation formulation, List<XFormulationInput> formulationInputList);
    void removeInput(XFormulationInput formulationInput);

}
