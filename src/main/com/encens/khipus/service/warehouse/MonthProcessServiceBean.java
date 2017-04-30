package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.MonthProcessValidException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherPendantException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.FinancesModulePK;
import com.encens.khipus.model.finances.FinancesModuleState;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.warehouse.WarehouseDefaultConstants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Stateless
@Name("monthProcessService")
@AutoCreate
public class MonthProcessServiceBean extends GenericServiceBean implements MonthProcessService {

    public Date getMothProcessDate(Date date) {
        FinancesModule financesModule = getFinancesModule(WarehouseDefaultConstants.getWarehouseFinancesModulePK());

        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        Date lastDayOfCurrentProcessMonth = DateUtils.lastDate(firstDayOfCurrentProcessMonth);

        if (DateUtils.isDayInMonth(firstDayOfCurrentProcessMonth, lastDayOfCurrentProcessMonth, date)) {
            return date;
        } else {
            return lastDayOfCurrentProcessMonth;
        }
    }

    public void closeCurrentProcessMonth() throws WarehouseVoucherPendantException, MonthProcessValidException {
        FinancesModule financesModule = getFinancesModule(WarehouseDefaultConstants.getWarehouseFinancesModulePK());

        if (existsPendantWarehouseVouchers(financesModule.getId().getCompanyNumber(), financesModule)) {
            log.debug("Cannot close the current process month, because exists some warehouse vouchers than are pendant.");
            throw new WarehouseVoucherPendantException();
        }

        Date firstDayOfCurrentProcessMonth = financesModule.getDate();
        Date lastDayOfCurrentProcessMonth = DateUtils.getLastDayOfMonth(firstDayOfCurrentProcessMonth);

        Date today = new Date();
        if (DateUtils.isDayInMonth(firstDayOfCurrentProcessMonth, lastDayOfCurrentProcessMonth, today)) {
            log.debug("Cannot close the current process month because it still valid.");
            throw new MonthProcessValidException();
        }

        Date firstDayOfmonth = DateUtils.getFirstDayOfMonth(today);
        financesModule.setDate(firstDayOfmonth);
        getEntityManager().merge(financesModule);
        getEntityManager().flush();

        log.debug("Current month was updated from " + firstDayOfCurrentProcessMonth + " to " + firstDayOfmonth);
    }

    @SuppressWarnings(value = "unchecked")
    private boolean existsPendantWarehouseVouchers(String companyNumber, FinancesModule financesModule) {
        Date startDate = financesModule.getDate();
        Date endDate = DateUtils.getLastDayOfMonth(startDate);

        List<WarehouseVoucher> pendantVouchers = getEntityManager().createNamedQuery("WarehouseVoucher.findByState").
                setParameter("companyNumber", companyNumber).
                setParameter("state", WarehouseVoucherState.PEN).
                setParameter("startDate", startDate).
                setParameter("endDate", endDate).getResultList();

        return null != pendantVouchers && !pendantVouchers.isEmpty();
    }

    private FinancesModule getFinancesModule(FinancesModulePK id) {
        FinancesModule financesModule;
        try {
            financesModule = findById(FinancesModule.class, id);
        } catch (EntryNotFoundException e) {
            financesModule = createFinancesModule(id);
        }

        return financesModule;
    }

    private FinancesModule createFinancesModule(FinancesModulePK id) {
        FinancesModule financesModule = new FinancesModule();
        financesModule.setId(id);
        Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(new Date());
        financesModule.setDate(firstDayOfMonth);
        financesModule.setCurrency(null);
        financesModule.setDescription("Inventory");
        financesModule.setDocumentTypeExtension(null);
        financesModule.setState(FinancesModuleState.VIG);
        getEntityManager().persist(financesModule);
        getEntityManager().flush();
        return financesModule;
    }
}