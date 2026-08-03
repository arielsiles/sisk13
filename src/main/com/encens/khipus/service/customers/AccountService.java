package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Credit service interface
 *
 * @author
 */

@Local
public interface AccountService {

    void createAccount(Account account);
    void updateAccount(Account account);
    List<VoucherDetail> getAccountDetailList(Account account);
    List<VoucherDetail> getPartnerDetailList(Partner partner);
    BigDecimal  calculateAccountBalance(Account account, Date startDate, Date endDate);
    List<VoucherDetail> getMovementAccountBetweenDates(Account account, Date startDate, Date endDate);
    List<Account> getAccountList();
    List<Account> getSavingsAccounts(SavingType savingType);
    List<Account> getSavingsAccounts(SavingType savingType, FinancesCurrencyType currencyType);

    /**
     * Cuentas de un tipo de ahorro cuyo periodo de vigencia se solapa con el rango dado.
     * <p/>
     * Deliberadamente NO filtra por <code>estado</code>: al renovar un DPF el sistema deja
     * la cuenta anterior en INACTIVE, y esa cuenta igual devengo intereses hasta su
     * vencimiento dentro del mes que se esta provisionando. Filtrar por estado activo
     * perderia esos dias. Lo que manda es la fecha de vencimiento.
     */
    List<Account> getSavingsAccountsByPeriod(SavingType savingType, Date startDate, Date endDate);
    List<Account> getAccountList(Partner partner);

}
