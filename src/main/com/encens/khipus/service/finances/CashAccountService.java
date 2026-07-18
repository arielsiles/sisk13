package com.encens.khipus.service.finances;

import com.encens.khipus.action.finances.dto.AccountLevelErrorDTO;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;

import javax.ejb.Local;
import java.util.List;

/**
 * CashAccountService
 *
 * @author
 * @version 2.0
 */
@Local
public interface CashAccountService {
    Boolean existsAccount(String accountCode);

    CashAccount findByAccountCode(String accountCode);

    Boolean isActiveAccount(String accountCode);

    public List<CashAccount> findCashAccountList();

    public List<CashAccount> findCashAccountListByType(CashAccountType accountType);

    Boolean createCashAccount(CashAccount cashAccount);

    public List<CashAccount> findCashAccountsUtil();

    /**
     * Cuentas cuya longitud de codigo no es 10 digitos. No se procesan en la
     * correccion; solo se informan para revision manual.
     */
    List<AccountLevelErrorDTO> findAccountFormatErrors();

    /**
     * Cuentas (de 10 digitos) cuya cta_raiz no coincide con los 2 primeros
     * digitos + 8 ceros. Se omiten las que tienen cta_raiz vacia.
     */
    List<AccountLevelErrorDTO> findRootAccountErrors();

    /**
     * Cuentas (de 10 digitos) cuya cta_niv3 no coincide con lo esperado segun
     * el nivel: vacio en niveles 1-2; 3 primeros digitos + 7 ceros en 3+.
     */
    List<AccountLevelErrorDTO> findLevel3AccountErrors();

    /**
     * Corrige cta_raiz y cta_niv3 de todas las cuentas de 10 digitos, en todas
     * las companias. Devuelve {registrosRaizCorregidos, registrosNivel3Corregidos}.
     */
    int[] fixAccountLevelErrors();

    /**
     * Devuelve true si el codigo de cuenta ya existe en cualquier compania.
     */
    boolean accountCodeExists(String accountCode);

    /**
     * Busca todas las tablas donde el codigo de cuenta esta referenciado (por
     * valor; el sistema no tiene FKs a arcgms). La lista se deriva del mapeo
     * JPA (@JoinColumn referencedColumnName="cuenta") + configuracion + la
     * jerarquia (cta_raiz/cta_niv3 de OTRAS cuentas). Lista vacia = sin uso.
     * Solo se consultan columnas realmente existentes en el esquema actual.
     */
    List<String> findAccountReferences(String accountCode);

    /**
     * Renombra el codigo de una cuenta (cambia la PK). SOLO debe llamarse tras
     * verificar con findAccountReferences que la cuenta no esta referenciada.
     */
    void renameCashAccount(String companyNumber, String oldAccountCode, String newAccountCode);

}
