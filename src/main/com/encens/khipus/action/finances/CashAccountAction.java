package com.encens.khipus.action.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.finances.CashAccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.Messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Name("cashAccountAction")
@Scope(ScopeType.CONVERSATION)
public class CashAccountAction extends GenericAction<CashAccount> {

    @In
    private CashAccountService cashAccountService;

    @Factory(value = "cashAccount", scope = ScopeType.STATELESS)
    public CashAccount initCashAccount() {
        return getInstance();
    }

    @Factory(value = "cashAccountTypes", scope = ScopeType.STATELESS)
    public CashAccountType[] getCashAccountTypes() {
        return CashAccountType.values();
    }

    @Factory(value = "financesCurrencyTypes", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypes() {
        return FinancesCurrencyType.values();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CashAccount instance){

        return super.select(instance);
    }

    @Override
    @End
    public String update() {
        try {
            getService().update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    public String create() {

        Boolean result = cashAccountService.createCashAccount(getInstance());

        if (result) {
            addCreatedMessage();
            return Outcome.SUCCESS;
        } else {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    public void clearRootAccount() {
        getInstance().setRootCashAccount(null);
    }

    public void clearLevel3Account() {
        getInstance().setCashAccountLeve3(null);
    }

    public void assignRootCashAccount(CashAccount cashAccount) {
        getInstance().setRootCashAccount(cashAccount);
        getInstance().setRootAccountCode(cashAccount.getAccountCode());
    }

    public void assignLevel3Account(CashAccount cashAccount) {
        getInstance().setCashAccountLeve3(cashAccount);
        getInstance().setAccountLevel3Code(cashAccount.getAccountCode());
    }

    // ------------------------------------------------------------------------
    // Renumerar (renombrar el codigo de cuenta = PK). Solo se permite si la
    // cuenta NO esta referenciada en ninguna parte (findAccountReferences).
    // El codigo de cuenta es la PK, por eso no se puede cambiar con el update
    // normal (merge); se hace con un UPDATE nativo de la PK.
    // ------------------------------------------------------------------------

    private String newAccountCode;
    private List<String> references = new ArrayList<String>();
    private boolean renamed = false;
    private String renameMessage;
    private boolean renameError;

    /**
     * Prepara el modal de renumeracion (estado limpio).
     */
    public void prepareRename() {
        newAccountCode = null;
        references = new ArrayList<String>();
        renamed = false;
        renameMessage = null;
        renameError = false;
    }

    /**
     * Mensaje del modal. No se usa FacesMessages a proposito: el panel global de
     * la plantilla tambien los pinta y el mensaje salia duplicado (una vez en la
     * pagina y otra dentro del modal).
     */
    private void setRenameMessage(String key, boolean error, Object... params) {
        String template = Messages.instance().get(key);
        renameMessage = (params.length > 0 && template != null)
                ? MessageFormat.format(template, params) : template;
        renameError = error;
    }

    /**
     * Valida el nuevo codigo y el uso de la cuenta; si esta libre, la renombra.
     * Si esta en uso, NO renombra y expone la lista de tablas en 'references'.
     */
    public void renameAccount() {
        references = new ArrayList<String>();
        renamed = false;
        renameMessage = null;
        renameError = false;

        String oldCode = getInstance().getId().getAccountCode();
        String company = getInstance().getId().getCompanyNumber();
        String code = newAccountCode != null ? newAccountCode.trim() : "";

        if (code.isEmpty()) {
            setRenameMessage("CashAccount.rename.emptyCode", true);
            return;
        }
        if (code.equals(oldCode)) {
            setRenameMessage("CashAccount.rename.sameCode", true);
            return;
        }
        if (!code.matches("\\d{10}")) {
            setRenameMessage("CashAccount.rename.invalidCode", true);
            return;
        }
        if (cashAccountService.accountCodeExists(code)) {
            setRenameMessage("CashAccount.rename.codeExists", true, code);
            return;
        }

        references = cashAccountService.findAccountReferences(oldCode);
        if (!references.isEmpty()) {
            setRenameMessage("CashAccount.rename.inUse", true);
            return;
        }

        cashAccountService.renameCashAccount(company, oldCode, code);
        // Recargar la instancia con el nuevo codigo para reflejarlo en pantalla.
        setInstance(cashAccountService.findByAccountCode(code));
        renamed = true;
        setRenameMessage("CashAccount.rename.done", false, oldCode, code);
    }

    public String getNewAccountCode() {
        return newAccountCode;
    }

    public void setNewAccountCode(String newAccountCode) {
        this.newAccountCode = newAccountCode;
    }

    public List<String> getReferences() {
        return references;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public String getRenameMessage() {
        return renameMessage;
    }

    public boolean isRenameError() {
        return renameError;
    }
}
