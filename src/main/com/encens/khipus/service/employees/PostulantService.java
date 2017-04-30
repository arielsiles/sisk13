package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.employees.*;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : postulantService, 27-11-2009 01:50:23 AM
 */
@Local
public interface PostulantService {
    void create(Postulant postulant, List<PostulantAcademicFormation> academicFormationList, List<Experience> experienceList, List<HourAvailable> hourAvailableList, List<Subject> subjectResultList, List<PostulantCharge> postulantChargeList) throws EntryDuplicatedException, ConcurrencyException, EntryNotFoundException;

}
