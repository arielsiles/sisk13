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
    // Validacion y autocompletado del codigo de cuenta (solo en alta).
    //
    // El codigo de 10 digitos se descompone en 6 niveles de longitud fija
    // {1,1,1,3,2,2}. El nivel de la cuenta es el ultimo segmento no-cero, y no
    // puede haber un segmento con valor despues de uno en cero (seria un hueco
    // en la jerarquia). Tipo y moneda se heredan de la cuenta padre: en los
    // datos actuales el tipo coincide con el padre en el 100% de los casos y la
    // moneda en el 99.8%.
    // ------------------------------------------------------------------------

    /** Longitud de cada nivel del codigo. */
    private static final int[] LEVEL_LENGTHS = {1, 1, 1, 3, 2, 2};
    /** Digitos significativos acumulados por nivel: nivel N usa CUT[N-1] digitos. */
    private static final int[] LEVEL_CUT = {1, 2, 3, 6, 8, 10};
    private static final int ACCOUNT_CODE_LENGTH = 10;

    private String accountCodeMessage;
    private boolean accountCodeError;

    /**
     * Valida el codigo tipeado y, si es valido, completa el resto del formulario.
     * Se dispara al salir del campo (onblur) en el alta.
     */
    public void analyzeAccountCode() {
        accountCodeMessage = null;
        accountCodeError = false;

        String code = getInstance().getAccountCode();
        code = code != null ? code.trim() : "";
        if (code.length() == 0) {
            return; // campo vacio: no hay nada que validar todavia
        }

        if (!code.matches("\\d{" + ACCOUNT_CODE_LENGTH + "}")) {
            setAccountCodeMessage("CashAccount.codeCheck.invalidFormat", true);
            return;
        }

        int level = levelOf(splitInLevels(code));
        if (level == 0) {
            setAccountCodeMessage("CashAccount.codeCheck.maskError", true);
            return;
        }

        if (cashAccountService.accountCodeExists(code)) {
            setAccountCodeMessage("CashAccount.codeCheck.exists", true, code);
            return;
        }

        // Padre inmediato: obligatorio salvo en nivel 1.
        CashAccount parent = null;
        if (level > 1) {
            String parentCode = padWithZeros(code.substring(0, LEVEL_CUT[level - 2]));
            parent = cashAccountService.findByAccountCode(parentCode);
            if (parent == null) {
                setAccountCodeMessage("CashAccount.codeCheck.parentMissing", true, parentCode);
                return;
            }
        }

        applyDerivedValues(code, level, parent);
    }

    /**
     * Completa los campos que se deducen del codigo. Todos quedan editables:
     * en los datos hay cuentas atipicas que no siguen la regla.
     */
    private void applyDerivedValues(String code, int level, CashAccount parent) {
        getInstance().setAccountLevel(level);
        getInstance().setActive(Boolean.TRUE);
        // Se marca sola solo en nivel 6. El nivel 5 tambien puede ser de
        // movimiento, pero se deja a criterio del usuario (el campo es editable).
        getInstance().setMovementAccount(level == 6);

        if (parent != null) {
            getInstance().setAccountType(parent.getAccountType());
            getInstance().setCurrency(parent.getCurrency());
        }

        String rootCode = padWithZeros(code.substring(0, LEVEL_CUT[1]));
        CashAccount root = cashAccountService.findByAccountCode(rootCode);
        if (root != null) {
            assignRootCashAccount(root);
        }

        String level3Code = null;
        if (level >= 3) {
            level3Code = padWithZeros(code.substring(0, LEVEL_CUT[2]));
            CashAccount level3 = cashAccountService.findByAccountCode(level3Code);
            if (level3 != null) {
                assignLevel3Account(level3);
            }
        } else {
            clearLevel3Account();
            getInstance().setAccountLevel3Code(null);
        }

        String level3Label = level3Code != null ? level3Code
                : Messages.instance().get("CashAccount.codeCheck.notApplicable");
        setAccountCodeMessage("CashAccount.codeCheck.ok", false, level, rootCode, level3Label);
    }

    private List<String> splitInLevels(String code) {
        List<String> segments = new ArrayList<String>();
        int from = 0;
        for (int length : LEVEL_LENGTHS) {
            segments.add(code.substring(from, from + length));
            from += length;
        }
        return segments;
    }

    /**
     * Nivel = ultimo segmento no-cero. Devuelve 0 si la mascara esta rota, es
     * decir si algun segmento anterior a ese esta en cero (hueco en el arbol).
     */
    private int levelOf(List<String> segments) {
        int last = 0;
        for (int i = 0; i < segments.size(); i++) {
            if (!isZero(segments.get(i))) {
                last = i + 1;
            }
        }
        if (last == 0) {
            return 0;
        }
        for (int i = 0; i < last; i++) {
            if (isZero(segments.get(i))) {
                return 0;
            }
        }
        return last;
    }

    private boolean isZero(String segment) {
        return segment.replace("0", "").length() == 0;
    }

    private String padWithZeros(String prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        while (builder.length() < ACCOUNT_CODE_LENGTH) {
            builder.append('0');
        }
        return builder.toString();
    }

    private void setAccountCodeMessage(String key, boolean error, Object... params) {
        String template = Messages.instance().get(key);
        accountCodeMessage = (params.length > 0 && template != null)
                ? MessageFormat.format(template, params) : template;
        accountCodeError = error;
    }

    public String getAccountCodeMessage() {
        return accountCodeMessage;
    }

    public boolean isAccountCodeError() {
        return accountCodeError;
    }

    /**
     * Segmentos del codigo actual, para mostrar la distribucion por niveles.
     * Se calcula del codigo en cada render (no se cachea) para que funcione
     * igual en alta -tras validar- y en edicion -al abrir la pantalla-.
     */
    public List<String> getAccountSegments() {
        String code = getInstance() != null ? getInstance().getAccountCode() : null;
        code = code != null ? code.trim() : "";
        return code.matches("\\d{" + ACCOUNT_CODE_LENGTH + "}") ? splitInLevels(code) : null;
    }

    /**
     * Etiquetas N1..N6. Lista paralela a getAccountSegments(): la vista las
     * recorre por separado, porque ui:repeat de Facelets 1.x (JSF 1.2) no
     * soporta varStatus y el indice quedaba siempre en 1.
     */
    public List<String> getAccountSegmentLabels() {
        if (getAccountSegments() == null) {
            return null;
        }
        String prefix = Messages.instance().get("CashAccount.codeCheck.levelShort");
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < LEVEL_LENGTHS.length; i++) {
            labels.add(prefix + (i + 1));
        }
        return labels;
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
