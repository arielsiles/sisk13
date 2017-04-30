package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.MonthProcessValidException;
import com.encens.khipus.exception.warehouse.WarehouseVoucherPendantException;
import com.encens.khipus.framework.service.GenericService;

import javax.ejb.Local;
import java.util.Date;

/**
 * @author
 * @version 2.0
 */
@Local
public interface MonthProcessService extends GenericService {
    Date getMothProcessDate(Date date);

    void closeCurrentProcessMonth() throws WarehouseVoucherPendantException, MonthProcessValidException;
}
