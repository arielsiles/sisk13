package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.FinancesModulePK;
import com.encens.khipus.model.finances.FinancesModuleState;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.fixedassets.FixedAssetDefaultConstants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.Calendar;
import java.util.Date;

/**
 * @author
 * @version 2.1
 */
@Stateless
@Name("fixedAssetMonthProcessService")
@AutoCreate
public class FixedAssetMonthProcessServiceBean extends GenericServiceBean implements FixedAssetMonthProcessService {

    public boolean isDateInFixedAssetMothProcess(Date date) {
        FinancesModule financesModule = getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());

        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        Date lastDayOfCurrentProcessMonth = DateUtils.getLastDayOfMonth(firstDayOfCurrentProcessMonth);

        return DateUtils.isDayInMonth(firstDayOfCurrentProcessMonth, lastDayOfCurrentProcessMonth, date);
    }

    public boolean isDateAfterFixedAssetMothProcess(Date date) {
        FinancesModule financesModule = getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());

        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        Date lastDayOfCurrentProcessMonth = DateUtils.getLastDayOfMonth(firstDayOfCurrentProcessMonth);

        return lastDayOfCurrentProcessMonth.before(date);
    }

    public boolean isDateAfterFixedAssetMothProcessInitDate(Date date) {
        FinancesModule financesModule = getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());

        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        System.out.println("----- DATE AFTER ------> firstDayOfCurrentProcessMonth: " + firstDayOfCurrentProcessMonth);
        System.out.println("----- DATE AFTER ------> firstDayOfCurrentProcessMonth.after: " + firstDayOfCurrentProcessMonth.after(date));
        //return firstDayOfCurrentProcessMonth.after(date);

        return true;
    }


    public boolean isDateOneOrMoreMonthsAfterFixedAssetMothProcess(Date date) {
        FinancesModule financesModule = getFinancesModule(FixedAssetDefaultConstants.getFixedAssetModulePK());
        Calendar compareDate = DateUtils.toDateCalendar(date);
        Date dayOfCurrentProcessMonth = financesModule.getDate();
        Calendar lastDayOfCurrentProcessMonth = DateUtils.toCalendar(dayOfCurrentProcessMonth);
        lastDayOfCurrentProcessMonth.set(
                Calendar.DAY_OF_MONTH,
                lastDayOfCurrentProcessMonth.getMaximum(Calendar.DAY_OF_MONTH)
        );
        return lastDayOfCurrentProcessMonth.after(compareDate);
    }

    public FinancesModule getFinancesModule(FinancesModulePK id) {
        FinancesModule financesModule;
        try {
            financesModule = findById(FinancesModule.class, id);
        } catch (EntryNotFoundException e) {
            financesModule = createFinancesModule(id, FixedAssetDefaultConstants.DESCRIPTION);
        }
        return financesModule;
    }

    @SuppressWarnings({"NullableProblems"})
    private FinancesModule createFinancesModule(FinancesModulePK id, String moduleDescription) {
        FinancesModule financesModule = new FinancesModule();
        financesModule.setId(id);
        Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(new Date());
        financesModule.setDate(firstDayOfMonth);
        financesModule.setCurrency(null);
        financesModule.setDescription(moduleDescription);
        financesModule.setDocumentTypeExtension(null);
        financesModule.setState(FinancesModuleState.VIG);
        getEntityManager().persist(financesModule);
        getEntityManager().flush();
        return financesModule;
    }
}