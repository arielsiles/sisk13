package com.encens.khipus.action.finances;

import com.encens.khipus.action.finances.dto.AccountLevelErrorDTO;
import com.encens.khipus.service.finances.CashAccountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Analiza y corrige los niveles del plan de cuentas (tabla arcgms):
 * columnas cta_raiz y cta_niv3, en todas las companias.
 *
 * Replica en la aplicacion la logica de los scripts
 * scripts/analyze_arcgms.py y scripts/analyze_arcgms_niv3.py, evitando el
 * paso de exportar a CSV: analiza directamente contra la base de datos y
 * ofrece corregir todos los errores en una sola operacion.
 *
 * La correccion (fixAll) solo se ejecuta cuando el usuario la confirma en
 * pantalla; la logica de escritura vive en CashAccountService.
 */
@Name("cashAccountLevelAnalysisAction")
@Scope(ScopeType.PAGE)
public class CashAccountLevelAnalysisAction {

    @In
    private CashAccountService cashAccountService;

    @In
    private FacesMessages facesMessages;

    private List<AccountLevelErrorDTO> formatErrors = new ArrayList<AccountLevelErrorDTO>();
    private List<AccountLevelErrorDTO> rootErrors = new ArrayList<AccountLevelErrorDTO>();
    private List<AccountLevelErrorDTO> level3Errors = new ArrayList<AccountLevelErrorDTO>();

    private boolean analyzed = false;

    /**
     * Ejecuta el analisis contra la base de datos y llena las tres listas de
     * errores. Se dispara al abrir el modal.
     */
    public void analyze() {
        formatErrors = cashAccountService.findAccountFormatErrors();
        rootErrors = cashAccountService.findRootAccountErrors();
        level3Errors = cashAccountService.findLevel3AccountErrors();
        analyzed = true;
    }

    /**
     * Corrige cta_raiz y cta_niv3 de todas las cuentas de 10 digitos con error
     * y vuelve a analizar para refrescar el modal.
     */
    public void fixAll() {
        int[] fixed = cashAccountService.fixAccountLevelErrors();
        analyze();

        int total = fixed[0] + fixed[1];
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "CashAccount.levelAnalysis.fixed", total, fixed[0], fixed[1]);

        // Tras corregir, las filas cuyo destino no existe siguen siendo errores.
        int review = getRequiresReviewCount();
        if (review > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "CashAccount.levelAnalysis.reviewWarn", review);
        }
    }

    public List<AccountLevelErrorDTO> getFormatErrors() {
        return formatErrors;
    }

    public List<AccountLevelErrorDTO> getRootErrors() {
        return rootErrors;
    }

    public List<AccountLevelErrorDTO> getLevel3Errors() {
        return level3Errors;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public int getFormatErrorCount() {
        return formatErrors == null ? 0 : formatErrors.size();
    }

    public int getRootErrorCount() {
        return rootErrors == null ? 0 : rootErrors.size();
    }

    public int getLevel3ErrorCount() {
        return level3Errors == null ? 0 : level3Errors.size();
    }

    private int countCorrectable(List<AccountLevelErrorDTO> errors) {
        int count = 0;
        if (errors != null) {
            for (AccountLevelErrorDTO error : errors) {
                if (error.isTargetExists()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Errores de raiz cuya cuenta destino existe (se corregiran).
     */
    public int getRootCorrectableCount() {
        return countCorrectable(rootErrors);
    }

    /**
     * Errores de nivel 3 cuya cuenta destino existe -o es vacio- (se corregiran).
     */
    public int getLevel3CorrectableCount() {
        return countCorrectable(level3Errors);
    }

    /**
     * Cantidad de registros que el boton "Corregir todo" corregira realmente
     * (solo aquellos cuyo destino existe; las de formato no se corrigen).
     */
    public int getCorrectableErrorCount() {
        return getRootCorrectableCount() + getLevel3CorrectableCount();
    }

    /**
     * Filas marcadas en rojo: su "valor correcto" no existe como cuenta, asi
     * que NO se corrigen y quedan hasta que se cree/corrija la cuenta destino.
     */
    public int getRequiresReviewCount() {
        return (getRootErrorCount() - getRootCorrectableCount())
                + (getLevel3ErrorCount() - getLevel3CorrectableCount());
    }

    public boolean isHasCorrectableErrors() {
        return getCorrectableErrorCount() > 0;
    }
}
