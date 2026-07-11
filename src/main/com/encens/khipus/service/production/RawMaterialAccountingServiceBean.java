package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.VoucherBuilder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Armado del comprobante contable de la quincena de acopio. Movido desde
 * RawMaterialPaySummaryReportAction.accountingPeriod para poder reusarlo desde el
 * flujo de generacion (Contabilizar) y coordinarlo con el commit de la deuda.
 */
@Name("rawMaterialAccountingService")
@Stateless
@AutoCreate
public class RawMaterialAccountingServiceBean extends ExtendedGenericServiceBean
        implements RawMaterialAccountingService {

    @In
    private RawMaterialPayRollService rawMaterialPayRollService;
    @In
    private TypeMovementProducerService typeMovementProducerService;
    @In
    private SalaryMovementProducerService salaryMovementProducerService;
    @In
    private ClientService clientService;
    @In
    private VoucherAccoutingService voucherAccoutingService;

    @Override
    public Voucher contabilizar(Date startDate, Date endDate, MetaProduct metaProduct, String glossPeriodo) {

        // Bloque QUINCENA (dias habiles): unico con diferencia, descuentos, retencion, IT/IUE,
        // reserva y GA. Domingos y excedentes son planillas puras (solo leche cruda / acreedores).
        RawMaterialPayRollServiceBean.Discounts discounts = rawMaterialPayRollService.getDiscounts(
                startDate, endDate, metaProduct, PayRollType.NORMAL, DayType.HABIL);

        Double totalMoneyCollected = discounts.mount;
        Double totalDifferencesMoney = rawMaterialPayRollService.getSumAdjustmentFromRecords(
                startDate, endDate, metaProduct, PayRollType.NORMAL, DayType.HABIL);
        Double totalMoneyBalance = totalMoneyCollected + totalDifferencesMoney;
        Double reservProducer = discounts.reserve;
        Double reserveGA = discounts.ga;

        Double iue, it, porcentageIUE;
        RawMaterialPayRoll totals = rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate, endDate, null, null);
        porcentageIUE = totals.getIue() / totals.getTaxRate();
        iue = discounts.retention * porcentageIUE;
        it = discounts.retention - iue;
        Double totalLiquid = discounts.liquid;

        RawMaterialPayRollServiceBean.Discounts domingos = rawMaterialPayRollService.getDiscounts(
                startDate, endDate, metaProduct, PayRollType.NORMAL, DayType.DOMINGO);
        RawMaterialPayRollServiceBean.Discounts excedenteQuincena = rawMaterialPayRollService.getDiscounts(
                startDate, endDate, metaProduct, PayRollType.EXCEDENTE, DayType.HABIL);
        RawMaterialPayRollServiceBean.Discounts excedenteDomingo = rawMaterialPayRollService.getDiscounts(
                startDate, endDate, metaProduct, PayRollType.EXCEDENTE, DayType.DOMINGO);
        Double domingoLiquid = domingos.liquid;
        Double excedenteLiquid = excedenteQuincena.liquid + excedenteDomingo.liquid;

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "REGISTRO ACOPIO DE LECHE " + glossPeriodo);
        voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        // ===== QUINCENA (habiles): estructura completa =====
        addDetail(voucher, Constants.ACCOUNT_LECHECRUDA, totalMoneyBalance, true, null, "ACOPIO QUINCENA (HABILES)");

        if (discounts.commission > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.commission, false, null, null);
        if (it > 0)
            addDetail(voucher, Constants.ACCOUNT_IT_RETENIDO, it, false, null, null);
        if (iue > 0)
            addDetail(voucher, Constants.ACCOUNT_IUE_RETENIDO, iue, false, null, null);
        if (totalLiquid > 0)
            addDetail(voucher, Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS, totalLiquid, false,
                    Constants.PROVIDER_CODE_PRODUCTORES, "LIQUIDO QUINCENA (HABILES)");

        // Veterinario -> Clientes Productores (por productor)
        if (discounts.veterinary > 0) {
            TypeMovementProducer type = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.VETE);
            List<SalaryMovementProducer> movs = salaryMovementProducerService.findSalaryMovementProducerList(startDate, endDate, type);
            for (SalaryMovementProducer m : movs) {
                Client client = clientService.findClientByIdNumber(m.getRawMaterialProducer().getIdNumber());
                addDetailClient(voucher, Constants.ACCOUNT_CLIENTESPRODUCTORES, m.getValor(), client);
            }
        }
        // Yogurt/Lacteos -> Clientes (por productor)
        if (discounts.yogurt > 0) {
            TypeMovementProducer type = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.LACT);
            List<SalaryMovementProducer> movs = salaryMovementProducerService.findSalaryMovementProducerList(startDate, endDate, type);
            for (SalaryMovementProducer m : movs) {
                Client client = clientService.findClientByIdNumber(m.getRawMaterialProducer().getIdNumber());
                addDetailClient(voucher, Constants.ACCOUNT_CLIENTES, m.getValor(), client);
            }
        }

        if (discounts.credit > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.credit, false, null, null);
        if (discounts.alcohol > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.alcohol, false, null, null);
        if (discounts.concentrated > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.concentrated, false, null, null);
        if (discounts.recip > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.recip, false, null, null);
        if (discounts.otherDiscount > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, discounts.otherDiscount, false, null, null);
        if (reservProducer > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, reservProducer, false, null, null);
        if (reserveGA > 0)
            addDetail(voucher, Constants.ACCOUNT_FONDOSCUSTODIA, reserveGA, false, null, null);

        // ===== DOMINGOS (puro) =====
        if (domingoLiquid > 0) {
            addDetail(voucher, Constants.ACCOUNT_LECHECRUDA, domingoLiquid, true, null, "ACOPIO DOMINGOS");
            addDetail(voucher, Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS, domingoLiquid, false,
                    Constants.PROVIDER_CODE_PRODUCTORES, "LIQUIDO DOMINGOS");
        }
        // ===== EXCEDENTES (puro) =====
        if (excedenteLiquid > 0) {
            addDetail(voucher, Constants.ACCOUNT_LECHECRUDA, excedenteLiquid, true, null, "ACOPIO EXCEDENTES");
            addDetail(voucher, Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS, excedenteLiquid, false,
                    Constants.PROVIDER_CODE_PRODUCTORES, "LIQUIDO EXCEDENTES");
        }

        voucherAccoutingService.saveVoucher(voucher);
        return voucher;
    }

    /** Detalle de asiento: debit=true -> al DEBE; false -> al HABER. */
    private void addDetail(Voucher voucher, String account, double amount, boolean debit,
                           String providerCode, String gloss) {
        VoucherDetail d = new VoucherDetail();
        d.setAccount(account);
        d.setDebit(debit ? BigDecimalUtil.toBigDecimal(amount) : BigDecimal.ZERO);
        d.setCredit(debit ? BigDecimal.ZERO : BigDecimalUtil.toBigDecimal(amount));
        d.setCurrency(FinancesCurrencyType.P);
        d.setExchangeAmount(BigDecimal.ONE);
        d.setDebitMe(BigDecimal.ZERO);
        d.setCreditMe(BigDecimal.ZERO);
        if (providerCode != null) d.setProviderCode(providerCode);
        if (gloss != null) d.setGloss(gloss);
        voucher.addVoucherDetail(d);
    }

    private void addDetailClient(Voucher voucher, String account, double amount, Client client) {
        VoucherDetail d = new VoucherDetail();
        d.setAccount(account);
        d.setDebit(BigDecimal.ZERO);
        d.setCredit(BigDecimalUtil.toBigDecimal(amount));
        d.setCurrency(FinancesCurrencyType.P);
        d.setExchangeAmount(BigDecimal.ONE);
        d.setDebitMe(BigDecimal.ZERO);
        d.setCreditMe(BigDecimal.ZERO);
        d.setClient(client);
        voucher.addVoucherDetail(d);
    }
}
