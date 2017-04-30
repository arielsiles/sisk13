package com.encens.khipus.service.academics;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PeriodService.java  19-ago-2010 15:54:55$
 */
@Local
public interface PeriodService {
    List<Integer> findAllPeriods();
}
