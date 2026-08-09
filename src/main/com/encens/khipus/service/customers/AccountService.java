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

    /**
     * Si el codigo ya lo usa otra cuenta. <code>excludedId</code> deja fuera a la cuenta
     * que se esta editando; en un alta va en null.
     */
    boolean existsAccountCode(String code, Long excludedId);

    /**
     * Siguiente numero de una serie de codigos de DPF, incrementado de forma atomica.
     *
     * @return el numero, o 0 si la secuencia no existe en <code>gensecuencia</code>.
     */
    long nextAccountCodeNumber(String sequenceName);
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

    /**
     * Movimientos del mayor de las cuentas indicadas, en un solo viaje a la base (una
     * consulta por certificado seria inaceptable en la provision mensual).
     * <p/>
     * Se traen TODOS, sin tope de fecha: quien los consume decide a que fecha corta. Poner
     * el tope aca hacia que el capital total de un certificado con un aumento posterior al
     * periodo saliera recortado, y el control contra <code>cuenta.capital</code> fallaba
     * por comparar el total final contra un total parcial.
     * <p/>
     * Excluye comprobantes anulados, igual que {@link #getAccountDetailList(Account)},
     * para que el saldo coincida con el que muestra la pantalla de la cuenta.
     *
     * @return filas <code>[idcuenta, fecha, debe, haber, debeMe, haberMe]</code>
     */
    List<Object[]> getAccountLedgerMovements(List<Long> accountIds);
    List<Account> getAccountList(Partner partner);

}
