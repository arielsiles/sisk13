package com.encens.khipus.service.production;

import com.encens.khipus.exception.finances.CompanyAccountNotConfiguredException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.CollectMaterialRevert;
import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.FinanceProviderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Stateless
@Name("collectMaterialService")
@AutoCreate
public class CollectMaterialServiceBean implements CollectMaterialService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private FinanceProviderService financeProviderService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private InventoryService inventoryService;

    @In
    private FacesMessages facesMessages;

    @Override
    public List<CollectMaterial> findCollectMaterialNoAccounting(Date startDate, Date endDate) {
        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                        "where c.date between :startDate and :endDate and c.state =:state")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .setParameter("state", CollectMaterialState.APR)
                .getResultList();

        return resultList;
    }

    @Override
    public List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate) {
        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                        "where c.date between :startDate and :endDate and c.state in (:stateApr, :stateConta)")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .setParameter("stateApr", CollectMaterialState.APR)
                        .setParameter("stateConta", CollectMaterialState.CONTA)
                .getResultList();

        return resultList;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate, String warehouseCode) {
        return em.createQuery(
                "select c from CollectMaterial c " +
                "join c.metaProduct mp " +
                "join mp.productItem pi " +
                "where c.date between :startDate and :endDate " +
                "and c.state in (:stateApr, :stateConta) " +
                "and pi.warehouseCode = :warehouseCode")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("stateApr", CollectMaterialState.APR)
                .setParameter("stateConta", CollectMaterialState.CONTA)
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
    }

    @Override
    public List<CollectMaterial> findApprovedCollectMaterialByCode(String productItemCode, Date startDate, Date endDate) {

        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                "where c.date between :startDate and :endDate and c.metaProduct.productItemCode = :productItemCode and c.state in (:stateApr, :stateConta)")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .setParameter("stateApr", CollectMaterialState.APR)
                .setParameter("stateConta", CollectMaterialState.CONTA)
                .getResultList();

        return resultList;
    }

    @Override
    public Voucher findVoucher(CollectMaterial collectMaterial) {
        if (collectMaterial.getVoucherId() == null) {
            return null;
        }
        return em.find(Voucher.class, collectMaterial.getVoucherId());
    }

    @Override
    public int getRevertWindowDays() {
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        if (companyConfiguration == null || companyConfiguration.getCollectMaterialRevertDays() == null) {
            return 0;
        }
        return companyConfiguration.getCollectMaterialRevertDays();
    }

    @Override
    public BigDecimal findCurrentBalance(CollectMaterial collectMaterial) {
        Inventory inventory = inventoryService.findInventoryByProductItemCode(
                collectMaterial.getMetaProduct().getProductItemCode());
        return inventory == null ? BigDecimal.ZERO : inventory.getUnitaryBalance();
    }

    @Override
    public BigDecimal findCurrentUnitCost(CollectMaterial collectMaterial) {
        Inventory inventory = inventoryService.findInventoryByProductItemCode(
                collectMaterial.getMetaProduct().getProductItemCode());
        return inventory == null ? BigDecimal.ZERO : inventory.getProductItem().getUnitCost();
    }

    /** Misma aritmetica que la reversa (InventoryServiceBean.decreaseProductItemAmount),
     *  pero sin escribir: es lo que el modal muestra antes de confirmar. */
    @Override
    public BigDecimal findProjectedUnitCost(CollectMaterial collectMaterial) {
        Inventory inventory = inventoryService.findInventoryByProductItemCode(
                collectMaterial.getMetaProduct().getProductItemCode());
        if (inventory == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal newQuantity = BigDecimalUtil.subtract(inventory.getUnitaryBalance(), collectMaterial.getBalanceWeight());
        if (BigDecimalUtil.isZeroOrNull(newQuantity)) {
            return BigDecimal.ZERO;
        }
        BigDecimal newAmount = BigDecimalUtil.subtract(inventory.getProductItem().getInvestmentAmount(),
                inventoryService.collectMaterialNetAmount(collectMaterial), 6);
        return BigDecimalUtil.divide(newAmount, newQuantity, 6);
    }

    /** Todo en una sola llamada para que inventario, estado y bitacora vivan en la misma
     *  transaccion: o queda todo, o no queda nada. */
    @Override
    public void revert(CollectMaterial collectMaterial, String reason) {

        CollectMaterialState previousState = collectMaterial.getState();
        Long voucherId = collectMaterial.getVoucherId();
        Date now = new Date();
        String user = Identity.instance().isLoggedIn() ? Identity.instance().getPrincipal().getName() : "unknown";

        Inventory inventory = inventoryService.findInventoryByProductItemCode(
                collectMaterial.getMetaProduct().getProductItemCode());
        BigDecimal balanceBefore  = inventory.getUnitaryBalance();
        BigDecimal unitCostBefore = inventory.getProductItem().getUnitCost();

        inventoryService.revertInventoryForCollectMaterial(collectMaterial);

        /** Se releen de la misma instancia gestionada, ya actualizada y flusheada. */
        BigDecimal balanceAfter  = inventory.getUnitaryBalance();
        BigDecimal unitCostAfter = inventory.getProductItem().getUnitCost();

        collectMaterial.setState(CollectMaterialState.PEN);
        collectMaterial.setAccountigFlag(Boolean.FALSE);
        collectMaterial.setVoucherId(null);
        collectMaterial.setRevertCount(collectMaterial.getRevertCount() == null ? 1 : collectMaterial.getRevertCount() + 1);
        collectMaterial.setLastRevertAt(now);
        collectMaterial.setLastRevertBy(user);
        collectMaterial.setLastRevertReason(reason);
        em.merge(collectMaterial);

        CollectMaterialRevert log = new CollectMaterialRevert();
        log.setCollectMaterial(collectMaterial);
        log.setDateTime(now);
        log.setUser(user);
        log.setReason(reason);
        log.setPreviousState(previousState);
        log.setVoucherId(voucherId);
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceAfter);
        log.setUnitCostBefore(unitCostBefore);
        log.setUnitCostAfter(unitCostAfter);
        em.persist(log);
        em.flush();
    }

    @Override
    public String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList , Date startDate, Date endDate) {

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String result = Outcome.FAIL;
        if (companyConfiguration == null) {
            return result;
        }

        /** Se valida todo antes de crear el primer asiento. Cada asiento consume su numero IMP
         *  en transaccion propia (FinancesSequenceService, REQUIRES_NEW): si algo fallara a
         *  mitad del rango, el rollback dejaria huecos en la numeracion. */
        Map<Long, FinancesEntity> providers = validateAccounting(collectMaterialList, companyConfiguration);
        if (providers == null) {
            return result;
        }

        for (CollectMaterial colMat:collectMaterialList) {

            String gloss = "INGRESO ALMACEN DE MATERIAS PRIMAS DEL " + DateUtils.format(colMat.getDate(), "dd/MM/yyyy") + " " +
                    colMat.getMetaProduct().getName() + ", " + "Proveedor: " + colMat.getProducer().getFullName() + ", " +
                    colMat.getCode() + ", Boleta: " + colMat.getTicket() + ", Form: " + colMat.getForm() + ", Chofer: " + colMat.getDriver();

            Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
            voucher.setDate(colMat.getDate());
            voucher.setDocumentType(Constants.IMP_VOUCHER_DOCTYPE);
            List<VoucherDetail> supplierDetailCashAcounts = new ArrayList<VoucherDetail>();

            FinancesEntity financesEntity = providers.get(colMat.getId());
            CashAccount warehouseCashAccount = colMat.getMetaProduct().getProductItem().getWarehouse().getWarehouseCashAccount();

            BigDecimal averageWeight = BigDecimalUtil.avg(colMat.getProviderWeight(), colMat.getBalanceWeight());
            BigDecimal weightTon    = BigDecimalUtil.divide(averageWeight, BigDecimalUtil.toBigDecimal(1000));
            BigDecimal amount       = BigDecimalUtil.multiply(weightTon, colMat.getPrice());
            BigDecimal totalAmount  = amount;

            System.out.println("------------------------------------------------");
            System.out.println("===> Peso Promedio: " + averageWeight);
            System.out.println("===> Peso Promedio Ton: " + weightTon);
            System.out.println("===> Price: " + colMat.getPrice());
            System.out.println("===> Monto 100%: " + amount);


            /** --Debe-- **/
            BigDecimal taxCreditFiscal = BigDecimal.ZERO;
            /** CF **/
            if (colMat.getHasInvoice()){
                taxCreditFiscal = BigDecimalUtil.multiply(amount, Constants.VAT);
                amount = BigDecimalUtil.subtract(amount, taxCreditFiscal);
                System.out.println("===> Tax CreditFiscal 13%: " + taxCreditFiscal);
                System.out.println("===> Monto 87%: " + amount);
            }

            VoucherDetail voucherDetailDev = VoucherDetailBuilder.newDebitVoucherDetail(
                    null, null, warehouseCashAccount, amount, FinancesCurrencyType.P, BigDecimal.ONE);

            //voucherDetailDev.setQuantityArt(colMat.getBalanceWeight());
            voucherDetailDev.setQuantityArt(averageWeight);
            voucherDetailDev.setProductItemCode(colMat.getMetaProduct().getProductItem().getProductItemCode());
            voucher.getDetails().add(voucherDetailDev);

            /** CF **/
            if (colMat.getHasInvoice()){
                VoucherDetail voucherDetailCF = VoucherDetailBuilder.newDebitVoucherDetail(
                        null, null, companyConfiguration.requireAccountPayableIVA(), taxCreditFiscal, FinancesCurrencyType.P, BigDecimal.ONE);
                voucher.getDetails().add(voucherDetailCF);
            }

            /** --Haber-- **/
            VoucherDetail supplierAccountOutput = VoucherDetailBuilder.newCreditVoucherDetail(
                    null, null, companyConfiguration.requireAccountPayableSupplier(), BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE);


            supplierAccountOutput.setProviderCode(financesEntity.getId().toString());
            supplierDetailCashAcounts.add(supplierAccountOutput);

            /** Regalia **/
            BigDecimal regaliaValue = BigDecimal.ZERO;
            if (colMat.getHasInvoice()) {
                regaliaValue = BigDecimalUtil.multiply(totalAmount, BigDecimalUtil.divide(colMat.getMetaProduct().getRegalia(), BigDecimalUtil.ONE_HUNDRED));
                VoucherDetail regaliaAccount = VoucherDetailBuilder.newCreditVoucherDetail(
                        null, null, companyConfiguration.requireAccountRegalia(), regaliaValue, FinancesCurrencyType.P, BigDecimal.ONE);
                supplierDetailCashAcounts.add(regaliaAccount);
            }

            /** CNS **/
            BigDecimal retentionCNSValue = BigDecimal.ZERO;
            if (!colMat.getProductiveZone().getHasCNS()){
                retentionCNSValue = BigDecimalUtil.multiply(totalAmount, companyConfiguration.getRetentionCNSValue());
                VoucherDetail retentionCNSAccount = VoucherDetailBuilder.newCreditVoucherDetail(
                        null, null, companyConfiguration.requireAccountRetentionCNS(), retentionCNSValue, FinancesCurrencyType.P, BigDecimal.ONE);
                supplierDetailCashAcounts.add(retentionCNSAccount);
            }

            BigDecimal supplierAccountValue = BigDecimalUtil.subtract(totalAmount, regaliaValue, retentionCNSValue);
            supplierAccountOutput.setCredit(supplierAccountValue);



            for (VoucherDetail voucherDetail:supplierDetailCashAcounts){
                voucher.getDetails().add(voucherDetail);
            }

            voucherAccoutingService.saveVoucher(voucher);

            /** Vinculo acopio -> asiento. Antes no se guardaba y el unico rastro quedaba en
             *  la glosa, que se pierde si alguien edita el codigo del acopio. */
            colMat.setVoucherId(voucher.getId());
            /** El estado va en esta misma transaccion. Antes lo cambiaba la accion con
             *  genericService.update (REQUIRES_NEW), que esperaba el lock de la fila que esta
             *  transaccion ya habia flusheado: con dos o mas acopios, lock wait timeout. */
            colMat.setState(CollectMaterialState.CONTA);

        }

        result = Outcome.SUCCESS;
        return  result;
    }

    /** Revisa todo lo que el asiento de cada acopio va a necesitar: cuentas de Preferencias de
     *  compania, cuenta contable del almacen, pesos y proveedor por CI. Avisa de cada problema
     *  encontrado y devuelve null si hubo alguno; si no, el proveedor de cada acopio. */
    private Map<Long, FinancesEntity> validateAccounting(List<CollectMaterial> collectMaterialList,
                                                        CompanyConfiguration companyConfiguration) {
        Map<Long, FinancesEntity> providers = new HashMap<Long, FinancesEntity>();
        /** Mensajes de configuracion: uno por cuenta o dato, no uno por acopio. */
        Set<String> configurationErrors = new LinkedHashSet<String>();
        boolean valid = true;

        requireConfigurationAccount(ACCOUNT_SUPPLIER, companyConfiguration, configurationErrors);
        for (CollectMaterial colMat : collectMaterialList) {
            if (Boolean.TRUE.equals(colMat.getHasInvoice())) {
                requireConfigurationAccount(ACCOUNT_IVA, companyConfiguration, configurationErrors);
                requireConfigurationAccount(ACCOUNT_REGALIA, companyConfiguration, configurationErrors);
                if (colMat.getMetaProduct().getRegalia() == null) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "CollectMaterial.accounting.regaliaNotDefined", colMat.getCode(), colMat.getMetaProduct().getName());
                    valid = false;
                }
            }
            if (!Boolean.TRUE.equals(colMat.getProductiveZone().getHasCNS())) {
                requireConfigurationAccount(ACCOUNT_CNS, companyConfiguration, configurationErrors);
                if (companyConfiguration.getRetentionCNSValue() == null) {
                    configurationErrors.add(MessageUtils.getMessage("CompanyConfiguration.retentionCNSValue"));
                }
            }

            if (colMat.getProviderWeight() == null && colMat.getBalanceWeight() == null) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "CollectMaterial.accounting.weightNotDefined", colMat.getCode());
                valid = false;
            }

            Warehouse warehouse = colMat.getMetaProduct().getProductItem().getWarehouse();
            if (!isValidAccount(warehouse.getWarehouseCashAccount())) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "CollectMaterial.accounting.warehouseWithoutAccount", colMat.getCode(),
                        colMat.getMetaProduct().getProductItem().getName(), warehouse.getName());
                valid = false;
            }

            String producerName = colMat.getProducer().getFullName();
            String idNumber = colMat.getProducer().getIdNumber();
            if (idNumber == null || idNumber.trim().length() == 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "CollectMaterial.accounting.producerWithoutIdNumber", colMat.getCode(), producerName);
                valid = false;
                continue;
            }
            List<FinancesEntity> found = em.createQuery("select e from FinancesEntity e where e.idNumber = :idNumber")
                    .setParameter("idNumber", idNumber.trim())
                    .getResultList();
            if (found.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "CollectMaterial.accounting.providerNotFound", colMat.getCode(), producerName, idNumber);
                valid = false;
            } else if (found.size() > 1) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "CollectMaterial.accounting.providerDuplicated", colMat.getCode(), producerName, idNumber, found.size());
                valid = false;
            } else {
                providers.put(colMat.getId(), found.get(0));
            }
        }

        for (String label : configurationErrors) {
            facesMessages.add(StatusMessage.Severity.ERROR,
                    MessageUtils.getMessage("CompanyConfiguration.account.notConfigured", label));
        }
        return valid && configurationErrors.isEmpty() ? providers : null;
    }

    private static final String ACCOUNT_SUPPLIER = "supplier";
    private static final String ACCOUNT_IVA = "iva";
    private static final String ACCOUNT_REGALIA = "regalia";
    private static final String ACCOUNT_CNS = "cns";

    /** Llama al require* de la cuenta (el mismo que usa el asiento) y, si falta, anota su etiqueta. */
    private void requireConfigurationAccount(String account, CompanyConfiguration companyConfiguration,
                                             Set<String> configurationErrors) {
        try {
            if (ACCOUNT_SUPPLIER.equals(account)) {
                companyConfiguration.requireAccountPayableSupplier();
            } else if (ACCOUNT_IVA.equals(account)) {
                companyConfiguration.requireAccountPayableIVA();
            } else if (ACCOUNT_REGALIA.equals(account)) {
                companyConfiguration.requireAccountRegalia();
            } else if (ACCOUNT_CNS.equals(account)) {
                companyConfiguration.requireAccountRetentionCNS();
            }
        } catch (CompanyAccountNotConfiguredException e) {
            configurationErrors.add(MessageUtils.getMessage(e.getLabelKey()));
        }
    }

    /** Cuenta asignada y existente en el plan de cuentas (la asociacion es LAZY: un codigo que
     *  ya no existe solo explota al tocar el proxy). */
    private boolean isValidAccount(CashAccount account) {
        if (account == null) {
            return false;
        }
        try {
            account.getAccountCode();
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    /* Se contabiliza todo el acopio por fecha, un solo asiento. */
    /*
    @Override
    public String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList , Date startDate, Date endDate) {
        String result = Outcome.FAIL;
        String gloss = "";
        if ( startDate.compareTo(endDate) == 0 )
            gloss = "INGRESO ALMACEN DE MATERIAS PRIMAS DEL " + DateUtils.format(startDate, "dd/MM/yyyy");
        else
            gloss = "INGRESO ALMACEN DE MATERIAS PRIMAS DEL " + DateUtils.format(startDate, "dd/MM/yyyy") + " AL " + DateUtils.format(endDate, "dd/MM/yyyy");

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
        voucher.setDocumentType(Constants.IA_VOUCHER_DOCTYPE);

        List<VoucherDetail> supplierDetailCashAcounts = new ArrayList<VoucherDetail>();
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        FinancesEntity financesEntity;

        for (CollectMaterial colMat:collectMaterialList) {
            financesEntity = financeProviderService.findByIdNumber(colMat.getProducer().getIdNumber());

            CashAccount warehouseCashAccount = colMat.getMetaProduct().getProductItem().getWarehouse().getWarehouseCashAccount();

            BigDecimal weightTon    = BigDecimalUtil.divide(colMat.getBalanceWeight(),BigDecimalUtil.toBigDecimal(1000));
            BigDecimal amount       = BigDecimalUtil.multiply(weightTon, colMat.getPrice());
            BigDecimal totalAmount  = amount;

            // --Debe--
            BigDecimal taxCreditFiscal = BigDecimal.ZERO;
            // CF
            if (colMat.getHasInvoice()){
                taxCreditFiscal = BigDecimalUtil.multiply(amount, Constants.VAT);
                amount = BigDecimalUtil.subtract(amount, taxCreditFiscal);
            }

            VoucherDetail voucherDetailDev = VoucherDetailBuilder.newDebitVoucherDetail(
                    null, null, warehouseCashAccount, amount, FinancesCurrencyType.P, BigDecimal.ONE);

            voucherDetailDev.setQuantityArt(colMat.getBalanceWeight());
            voucherDetailDev.setProductItemCode(colMat.getMetaProduct().getProductItem().getProductItemCode());
            voucher.getDetails().add(voucherDetailDev);

            // CF
            if (colMat.getHasInvoice()){
                VoucherDetail voucherDetailCF = VoucherDetailBuilder.newDebitVoucherDetail(
                        null, null, companyConfiguration.requireAccountPayableIVA(), taxCreditFiscal, FinancesCurrencyType.P, BigDecimal.ONE);
                voucher.getDetails().add(voucherDetailCF);
            }

            // --Haber--
            VoucherDetail supplierAccountOutput = VoucherDetailBuilder.newCreditVoucherDetail(
                    null, null, companyConfiguration.requireAccountPayableSupplier(), BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE);


            supplierAccountOutput.setProviderCode(financesEntity.getId().toString());
            supplierDetailCashAcounts.add(supplierAccountOutput);

            // Regalia
            BigDecimal regaliaValue = BigDecimal.ZERO;
            if (colMat.getHasInvoice()) {
                regaliaValue = BigDecimalUtil.multiply(totalAmount, BigDecimalUtil.divide(colMat.getMetaProduct().getRegalia(), BigDecimalUtil.ONE_HUNDRED));
                VoucherDetail regaliaAccount = VoucherDetailBuilder.newCreditVoucherDetail(
                        null, null, companyConfiguration.requireAccountRegalia(), regaliaValue, FinancesCurrencyType.P, BigDecimal.ONE);
                supplierDetailCashAcounts.add(regaliaAccount);
            }

            // CNS
            BigDecimal retentionCNSValue = BigDecimal.ZERO;
            if (!colMat.getProductiveZone().getHasCNS()){
                retentionCNSValue = BigDecimalUtil.multiply(totalAmount, companyConfiguration.getRetentionCNSValue());
                VoucherDetail retentionCNSAccount = VoucherDetailBuilder.newCreditVoucherDetail(
                        null, null, companyConfiguration.requireAccountRetentionCNS(), retentionCNSValue, FinancesCurrencyType.P, BigDecimal.ONE);
                supplierDetailCashAcounts.add(retentionCNSAccount);
            }

            BigDecimal supplierAccountValue = BigDecimalUtil.subtract(totalAmount, regaliaValue, retentionCNSValue);
            supplierAccountOutput.setCredit(supplierAccountValue);

        }

        //Collections.sort(supplierDetailCashAcounts, new Comparator<VoucherDetail>() {
        //    @Override
        //    public int compare(VoucherDetail o1, VoucherDetail o2) {
        //        return o1.getAccount().compareTo(o2.getAccount());
        //    }
        //});

        for (VoucherDetail voucherDetail:supplierDetailCashAcounts){
            voucher.getDetails().add(voucherDetail);
        }

        voucher.setDate(new Date());
        voucherAccoutingService.saveVoucher(voucher);
        result = Outcome.SUCCESS;
        return  result;
    }*/

    public CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
    }


    @Override
    public List<Object[]> findCollectMaterial() {
        List<Object[]> resultList = em.createQuery("select m.name, sum(c.balanceWeight) as peso " +
                        "from CollectMaterial c " +
                        "join c.metaProduct m " +
                        "group by m.name")
                .getResultList();

        return resultList;
    }

    @Override
    public List<Object[]> findCollectMaterialByProducer(){
        List<Object[]> resultList = em.createQuery("select p.firstName as name, sum(c.balanceWeight) as peso " +
                        " from CollectMaterial c " +
                        " left join c.producer p " +
                        " group by p.firstName ")
                .getResultList();

        return resultList;
    }

    @Override
    public List<Object[]> findCollectMaterialByZone(){
        List<Object[]> resultList = em.createQuery("select p.name, sum(c.balanceWeight) as peso " +
                        " from CollectMaterial c " +
                        " left join c.productiveZone p " +
                        " group by p.name ")
                .getResultList();

        return resultList;
    }

}
