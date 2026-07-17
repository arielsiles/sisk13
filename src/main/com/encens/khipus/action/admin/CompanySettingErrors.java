package com.encens.khipus.action.admin;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Marca que tab de "Preferencias de compania" tiene errores de validacion.
 * <p>
 * El form tiene ~123 campos repartidos en 7 tabs. Cuando la validacion falla,
 * JSF renderiza el error dentro del tab del campo; con los otros tabs ocultos,
 * el usuario veia el mensaje al tope pero tenia que abrir tab por tab para
 * encontrar cual era.
 * <p>
 * Se resuelve del lado del servidor y no con JavaScript sobre el DOM de
 * rich:tabPanel: durante el render, FacesContext ya sabe que clientIds tienen
 * mensajes, asi que basta con saber que campo vive en que tab. Ese mapa se
 * genera de la misma fuente que la pantalla, asi no se desincronizan.
 * <p>
 * NOTA: los clientId llegan como "userSettingForm:&lt;decorateId&gt;:&lt;inputId&gt;"
 * (s:decorate es NamingContainer), por eso se compara por segmento y no por
 * igualdad: el id del s:decorate es "&lt;campo&gt;Field" y el del div "&lt;campo&gt;Div".
 */
@Name("companySettingErrors")
@Scope(ScopeType.EVENT)
public class CompanySettingErrors {

    private static final Map<String, Set<String>> TAB_FIELDS = new HashMap<String, Set<String>>();

    static {
        TAB_FIELDS.put("company", new HashSet<String>(Arrays.asList(
            "title",
            "subTitle",
            "companyName",
            "systemName",
            "locationName",
            "exchangeRateBalanceCostCenter",
            "headerLogo",
            "loginLogo")));
        TAB_FIELDS.put("accounts", new HashSet<String>(Arrays.asList(
            "balanceExchangeRateAccount",
            "adjustmentForInflationAccount",
            "advancePaymentForeignCurrencyAccount",
            "advancePaymentNationalCurrencyAccount",
            "depositInTransitForeignCurrencyAccount",
            "depositInTransitNationalCurrencyAccount",
            "fixedAssetInTransitAccount",
            "foreignCurrencyVATFiscalCreditAccount",
            "nationalCurrencyVATFiscalCreditAccount",
            "nationalCurrencyVATFiscalCreditTransientAccount",
            "provisionByTangibleFixedAssetObsolescenceAccount",
            "warehouseForeignCurrencyAccount",
            "warehouseNationalCurrencyAccount",
            "warehouseForeignCurrencyTransientAccount",
            "warehouseNationalCurrencyTransientAccount",
            "warehouseNationalCurrencyTransientAccount1",
            "warehouseNationalCurrencyTransientAccount2",
            "iueRetention",
            "itRetention",
            "ctaCostPT",
            "ctaAlmPT",
            "ctaAlmPTAG",
            "ctaCostPV",
            "ctaAlmPV",
            "wasteAccount",
            "averageAccount",
            "lowAccount",
            "reworkAccount",
            "savingsBankAccount",
            "veterinaryCashAccount",
            "generalCashAccountNational",
            "fixedTermInterestNationalCurrency",
            "fixedAssetProvidersAccount",
            "transactionTaxExpense",
            "fiscalDebitLiability",
            "transactionTaxPayable",
            "primarySaleProduct",
            "secondarySaleProduct",
            "commissionSalesCashAccount",
            "accountPayableSupplier",
            "accountPayableIVA",
            "accountRegalia",
            "accountRetentionCNS",
            "accountBalanceSheet1",
            "accountBalanceSheet2",
            "accountBalanceSheet3",
            "accountBalanceSheet4",
            "accountBalanceSheet5",
            "lossCashAccount",
            "profitCashAccount",
            "defaultAccountPurchaseOrder",
            "nationalBankAccountForPayment",
            "foreignBankAccountForPayment")));
        TAB_FIELDS.put("taxes", new HashSet<String>(Arrays.asList(
            "it",
            "iue",
            "ivaTaxValue",
            "netValue",
            "itTaxValue",
            "retentionCNSValue")));
        TAB_FIELDS.put("billing", new HashSet<String>(Arrays.asList(
            "oneLegend",
            "onlineLegend",
            "offlineLegend",
            "invoiceAnnulDate",
            "measureUnitsURL",
            "productsAndServicesURL",
            "activitiesURL",
            "nitVerificationURL",
            "validateOfflineBillPackagesURL",
            "processOfflineBillPackagesURL",
            "prepareOfflineBillPackagesURL",
            "onlineModeURL",
            "offlineModeURL",
            "significantEventURL",
            "checkBillingModeURL",
            "createbillURL",
            "cancelbillURL",
            "qrcodeURL",
            "connectionTestURL")));
        TAB_FIELDS.put("employees", new HashSet<String>(Arrays.asList(
            "hrsWorkingDay",
            "basicBasedChristmasPayroll",
            "contractModificationAuthorization",
            "contractModificationCode",
            "retentionForLoanAndAdvance",
            "jobCategoryDLH",
            "jobCategoryDTH",
            "kindOfSalaryDLH",
            "kindOfSalaryDTH",
            "defaultProfessorsCharge",
            "defaultSalutationForMan",
            "defaultSalutationForWoman",
            "defaultDocumentType",
            "scheduleEvaluationRedirectURL",
            "studentScheduleEvaluationRedirectURL",
            "teacherScheduleEvaluationRedirectURL",
            "careerManagerScheduleEvaluationRedirectURL",
            "autoEvaluationScheduleEvaluationRedirectURL",
            "unisueldoEmail")));
        TAB_FIELDS.put("users", new HashSet<String>(Arrays.asList(
            "defaultSystemUserNumber",
            "defaultAccountancyUser",
            "defaultAccountancyUserProduction",
            "defaultTreasuryUser",
            "defaultPayableFinanceUser",
            "defaultPurchaseOrderRemakePaymentUserNumber",
            "defaultPurchaseOrderRemakeYear")));
        TAB_FIELDS.put("parameters", new HashSet<String>(Arrays.asList(
            "purchaseOrderCodificationEnabled",
            "treasuryDocumentsAuthorizationEnabled",
            "payablesDocumentsAuthorizationEnabled",
            "dispatchInventoryControl",
            "dealerParameter",
            "unitPriceMilk",
            "cashBoxDocumentType",
            "paymentDocumentOC",
            "documentFixedAssetOC")));
    }

    /**
     * @param tab clave del tab (company, accounts, taxes, billing, employees,
     *            users, parameters).
     * @return true si algun campo de ese tab tiene un mensaje de error.
     */
    public boolean isInError(String tab) {
        Set<String> fields = TAB_FIELDS.get(tab);
        if (fields == null) {
            return false;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return false;
        }
        Iterator<String> clientIds = context.getClientIdsWithMessages();
        while (clientIds.hasNext()) {
            String clientId = clientIds.next();
            if (clientId == null) {
                continue; // mensaje global, no pertenece a ningun tab
            }
            for (String segment : clientId.split(":")) {
                if (fields.contains(stripSuffix(segment))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String stripSuffix(String segment) {
        if (segment.endsWith("Field")) {
            return segment.substring(0, segment.length() - "Field".length());
        }
        if (segment.endsWith("Div")) {
            return segment.substring(0, segment.length() - "Div".length());
        }
        return segment;
    }
}
