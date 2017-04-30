package com.encens.khipus.service.fixedassets;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.FinancesModulePK;

import javax.ejb.Local;
import java.util.Date;

/**
 * @author
 * @version 2.1
 */
@Local
public interface FixedAssetMonthProcessService extends GenericService {

    boolean isDateInFixedAssetMothProcess(Date date);

    boolean isDateAfterFixedAssetMothProcess(Date date);

    FinancesModule getFinancesModule(FinancesModulePK id);

    boolean isDateOneOrMoreMonthsAfterFixedAssetMothProcess(Date date);

    boolean isDateAfterFixedAssetMothProcessInitDate(Date date);
}
