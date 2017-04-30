package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.FinalEvaluationForm;
import com.encens.khipus.model.employees.FinalEvaluationFormType;
import com.encens.khipus.util.TextUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: FinalEvaluationFormServiceBean.java  16-jun-2010 12:35:43$
 */
@Name("finalEvaluationFormService")
@Stateless
@AutoCreate
public class FinalEvaluationFormServiceBean extends GenericServiceBean implements FinalEvaluationFormService {
    @Logger
    private Log log;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        FinalEvaluationForm finalEvaluationForm = (FinalEvaluationForm) entity;
        if (TextUtil.isEmpty(finalEvaluationForm.getMethodology())) {
            FinalEvaluationForm currentForm = listEm.find(FinalEvaluationForm.class, finalEvaluationForm.getId());
            if (!TextUtil.isEmpty(currentForm.getMethodology())) {
                Text text = getEntityManager().find(Text.class, currentForm.getMethodology().getId());
                getEntityManager().remove(text);
            }
        }
        super.update(entity);
    }

    /**
     * Find final evaluation form by cycle and type
     *
     * @param cycle
     * @param finalEvaluationFormType
     * @return FinalEvaluationForm
     */
    public FinalEvaluationForm getFinalEvaluationFormByCycleAndType(Cycle cycle, FinalEvaluationFormType finalEvaluationFormType) {
        FinalEvaluationForm finalEvaluationForm = null;
        try {
            finalEvaluationForm = (FinalEvaluationForm) getEntityManager().createNamedQuery("FinalEvaluationForm.findByCycleType").
                    setParameter("cycle", cycle).
                    setParameter("finalEvaluationFormType", finalEvaluationFormType).
                    getSingleResult();
        } catch (Exception e) {
            log.debug("Error in find FinalEvaluationForm....", e);
        }
        return finalEvaluationForm;
    }

    /**
     * Return a boolean value for check the duplicated FinalEvaluationForms counted by cycle and type
     *
     * @param finalEvaluationForm the final evaluation form
     * @return Boolean
     */
    public Boolean isDuplicatedByCycleAndType(FinalEvaluationForm finalEvaluationForm) {
        Long countResult = (finalEvaluationForm.getId() == null) ?
                (Long) getEntityManager().createNamedQuery("FinalEvaluationForm.countByCycleType").
                        setParameter("cycle", finalEvaluationForm.getCycle()).
                        setParameter("type", finalEvaluationForm.getType()).
                        getSingleResult() :
                (Long) getEntityManager().createNamedQuery("FinalEvaluationForm.countByCycleTypeAndFinalForm").
                        setParameter("finalForm", finalEvaluationForm).
                        setParameter("cycle", finalEvaluationForm.getCycle()).
                        setParameter("type", finalEvaluationForm.getType()).
                        getSingleResult();
        return countResult != null && countResult > 0;
    }
}
