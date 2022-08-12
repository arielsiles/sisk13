package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.ApprovalWarehouseVoucherService;
import com.encens.khipus.service.warehouse.WarehouseAccountEntryService;
import com.encens.khipus.service.warehouse.WarehouseVoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseVoucherAction")
@Scope(ScopeType.CONVERSATION)
public class WarehouseVoucherAction extends GenericAction<WarehouseVoucher> {

    private Date startDate = new Date();
    private Date endDate  = new Date();
    private WarehouseDocumentType documentType;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    ApprovalWarehouseVoucherService approvalWarehouseVoucherService;
    @In
    WarehouseVoucherService warehouseVoucherService;

    @Factory(value = "warehouseVoucher", scope = ScopeType.STATELESS)
    public WarehouseVoucher initWarehouseVoucher() {
        return getInstance();
    }

    @Factory(value = "warehouseVoucherStates", scope = ScopeType.STATELESS)
    public WarehouseVoucherState[] getWarehouseVoucherStates() {
        return WarehouseVoucherState.values();
    }



    /** Vales de P.T. **/
    public void processVouchersWithoutAccounting() throws CompanyConfigurationNotFoundException {

        List<WarehouseVoucher> warehouseVoucherList = warehouseAccountEntryService.getVouchersWithoutAccounting(startDate, endDate);
        List<WarehouseVoucher> warehouseVoucherTPList = new ArrayList<WarehouseVoucher>();

        for (WarehouseVoucher warehouseVoucher : warehouseVoucherList){
            if (warehouseVoucher.getOperation().equals(VoucherOperation.BA)){
                createAccountingForVoucherBA(warehouseVoucher, startDate, endDate);
            }
            if (warehouseVoucher.getOperation().equals(VoucherOperation.DE)){
                createAccountingForVoucherDE(warehouseVoucher, startDate, endDate);
            }
            if (warehouseVoucher.getOperation().equals(VoucherOperation.TP)){
                //createAccountingForVoucherTP(warehouseVoucher, startDate, endDate);
                warehouseVoucherTPList.add(warehouseVoucher);
            }
        }
        if (warehouseVoucherTPList.size() > 0)
            createAccountingForVoucherTP(warehouseVoucherTPList, startDate, endDate);

        /** Contabilizando pedidos transferencia de almacen **/
        System.out.println("========> Contabilizando vales-pedidos transferencia de almacen <=============");
        /*List<CustomerOrder> transferOrderList =  warehouseAccountEntryService.getTransferOrderList(startDate, endDate);
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);

        for (CustomerOrder customerOrder : transferOrderList){
            System.out.println("-----> Pedido: " + customerOrder.getCode() + " : " + customerOrder.getTotalAmount() + " $bs");
            createAccountingForTransferOrders(customerOrder, unitCostMilkProducts);
        }*/

        createAccountingForTransferWarehouseVouchers(startDate, endDate);

    }

    public void createAccountingForTransferWarehouseVouchers(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        List<WarehouseVoucher> transferWarehouseVoucherList = warehouseAccountEntryService.getVouchersFromTransferCustomerOrder(startDate, endDate);

        if (transferWarehouseVoucherList.size() > 0){

            HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

            String ctaAlmPT   = companyConfiguration.getCtaAlmPT().getAccountCode();
            String ctaAlmPTAG = companyConfiguration.getCtaAlmPTAG().getAccountCode();

            Voucher voucher = new Voucher();
            voucher.setDate(endDate);
            voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
            voucher.setGloss("Transferencia de almacen pedidos del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));

            /*for (WarehouseVoucher warehouseVoucher : transferWarehouseVoucherList){

                for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
                    BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
                    BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

                    VoucherDetail voucherDetailDebit = new VoucherDetail(ctaAlmPTAG, amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                            movementDetail.getProductItemCode(), movementDetail.getQuantity());

                    voucher.getDetails().add(voucherDetailDebit);
                }

                for (ArticleOrder articleOrder : warehouseVoucher.getTransferCustomerOrder().getArticleOrderList()){
                    BigDecimal unitCost = unitCostMilkProducts.get(articleOrder.getProductItem().getProductItemCode());
                    BigDecimal amount = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getQuantity()), unitCost, 2);

                    VoucherDetail voucherDetailCredit = new VoucherDetail(ctaAlmPT, BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
                            articleOrder.getProductItem().getProductItemCode(), BigDecimalUtil.toBigDecimal(articleOrder.getQuantity()));

                    voucher.getDetails().add(voucherDetailCredit);
                }
            }*/


            /* Consolidando productos y cantidades usando HashMap */
            HashMap<String, BigDecimal> transferProducts = new HashMap<String, BigDecimal>();
            HashMap<String, BigDecimal> transferProductsPT = new HashMap<String, BigDecimal>();

            for (WarehouseVoucher warehouseVoucher : transferWarehouseVoucherList){
                for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
                    if (transferProducts.containsKey(movementDetail.getProductItemCode())){
                        BigDecimal quantity = transferProducts.get(movementDetail.getProductItemCode());
                        quantity = BigDecimalUtil.sum(quantity, movementDetail.getQuantity());
                        transferProducts.put(movementDetail.getProductItemCode(), quantity);
                    }else
                        transferProducts.put(movementDetail.getProductItemCode(), movementDetail.getQuantity());
                }

                System.out.println("-------> Transferencia de pedidos de Almacen");
                System.out.println(warehouseVoucher.getNumber() + " - " + warehouseVoucher.getDate());

                for (ArticleOrder articleOrder : warehouseVoucher.getTransferCustomerOrder().getArticleOrderList()){
                    if (transferProductsPT.containsKey(articleOrder.getCodArt())){
                        BigDecimal quantity = transferProductsPT.get(articleOrder.getCodArt());
                        quantity = BigDecimalUtil.sum(quantity, BigDecimalUtil.toBigDecimal(articleOrder.getQuantity()));
                        transferProductsPT.put(articleOrder.getCodArt(), quantity);
                    }else
                        transferProductsPT.put(articleOrder.getCodArt(), BigDecimalUtil.toBigDecimal(articleOrder.getQuantity()));
                }
            }

            for (Map.Entry<String, BigDecimal> entry : transferProducts.entrySet() ){
                String cod_art      = entry.getKey();
                BigDecimal quantity = entry.getValue();
                BigDecimal unitCost = unitCostMilkProducts.get(cod_art);
                BigDecimal amount = BigDecimalUtil.multiply(quantity, unitCost, 2);

                VoucherDetail voucherDetailDebit = new VoucherDetail(ctaAlmPTAG, amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE, cod_art, quantity);
                voucher.getDetails().add(voucherDetailDebit);
            }

            for (Map.Entry<String, BigDecimal> entry : transferProductsPT.entrySet() ){
                String cod_art      = entry.getKey();
                BigDecimal quantity = entry.getValue();
                BigDecimal unitCost = unitCostMilkProducts.get(cod_art);
                BigDecimal amount = BigDecimalUtil.multiply(quantity, unitCost, 2);

                VoucherDetail voucherDetailCredit = new VoucherDetail(ctaAlmPT, BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE, cod_art, quantity);
                voucher.getDetails().add(voucherDetailCredit);
            }

            voucherAccoutingService.saveVoucher(voucher);

            for (WarehouseVoucher warehouseVoucher : transferWarehouseVoucherList){
                warehouseVoucher.setVoucher(voucher);
                approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
            }
        }
    }

    public void createAccountingForVoucherBA(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.SA_VOUCHER_DOCTYPE);
        voucher.setGloss("BAJA DE PRODUCTO, " + warehouseVoucher.getInventoryMovementList().get(0).getDescription());

        VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getLowAccount().getAccountCode(), BigDecimal.ZERO, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,null, null);
        voucher.getDetails().add(voucherDetailDebit);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
            System.out.println("=====> BAJA: " + movementDetail.getProductItem().getFullName());
            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

            //VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
            VoucherDetail voucherDetailCredit = new VoucherDetail(movementDetail.getWarehouse().getCashAccount(),
            BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
            movementDetail.getProductItemCode(), movementDetail.getQuantity());

            voucher.getDetails().add(voucherDetailCredit);
            totalAmount = BigDecimalUtil.sum(totalAmount, amount, 2);
        }

        voucherDetailDebit.setDebit(totalAmount);
        voucherAccoutingService.saveVoucher(voucher);
        warehouseVoucher.setVoucher(voucher);
        approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
    }

    public void createAccountingForVoucherDE(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
        voucher.setGloss("DEVOLUCION/ENTRADA PT, " + warehouseVoucher.getInventoryMovementList().get(0).getDescription());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
            System.out.println("------> VOUCHER DEV: " + movementDetail.getProductItem().getFullName());
            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            System.out.println("------> unitCost: " + unitCost);
            System.out.println("------> Quantity: " + movementDetail.getQuantity());

            /** todo: Si unitCost es null, busca en los periodos anteriores **/
            if (unitCost == null){
                unitCost = voucherAccoutingService.getUnitCost_lastPeriod(movementDetail.getProductItemCode());
            }

            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);
            //VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
            VoucherDetail voucherDetailDebit = new VoucherDetail(movementDetail.getWarehouse().getCashAccount(),
                    amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity());

            voucher.getDetails().add(voucherDetailDebit);
            totalAmount = BigDecimalUtil.sum(totalAmount, amount, 2);
        }

        //VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaCostPT().getAccountCode(),
        VoucherDetail voucherDetailCredit = new VoucherDetail(warehouseVoucher.getWarehouse().getCashAccountCost(),
                BigDecimal.ZERO, totalAmount, FinancesCurrencyType.P, BigDecimal.ONE,null, null);
        voucher.getDetails().add(voucherDetailCredit);

        voucherAccoutingService.saveVoucher(voucher);
        warehouseVoucher.setVoucher(voucher);
        approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
    }

    public void createAccountingForVoucherTP(List<WarehouseVoucher> warehouseVoucherList, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);
        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.SA_VOUCHER_DOCTYPE);
        voucher.setGloss("TRANSFERENCIA DE PRODUCTO, DEL " + DateUtils.format(startDate, "dd/MM/yyyy") + " AL " + DateUtils.format(endDate, "dd/MM/yyyy"));

        for (WarehouseVoucher warehouseVoucher:warehouseVoucherList) {
            for (InventoryMovement inventoryMovement:warehouseVoucher.getInventoryMovementList()) {
                for (MovementDetail movementDetail:inventoryMovement.getMovementDetailList()) {
                    if (movementDetail.getMovementType().equals(MovementDetailType.S)) {

                        System.out.println("------> 1. " + movementDetail.getProductItem().getFullName());
                        System.out.println("------> 2. " + warehouseVoucher.getNumber() + " - " + warehouseVoucher.getGloss());

                        BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
                        BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

                        VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                                BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE, movementDetail.getProductItemCode(), movementDetail.getQuantity());

                        voucher.getDetails().add(voucherDetailCredit);
                    }

                    if (movementDetail.getMovementType().equals(MovementDetailType.E)) {
                        WarehouseVoucher origin = warehouseVoucher.getOrigin();
                        String articleCode  = origin.getInventoryMovementList().get(0).getMovementDetailList().get(0).getProductItemCode();
                        BigDecimal quantity = origin.getInventoryMovementList().get(0).getMovementDetailList().get(0).getQuantity();
                        BigDecimal unitCost = unitCostMilkProducts.get(articleCode);
                        BigDecimal amount = BigDecimalUtil.multiply(quantity, unitCost, 2);

                        VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                                amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                                movementDetail.getProductItemCode(), movementDetail.getQuantity());

                        voucher.getDetails().add(voucherDetailDebit);
                    }
                }
            }
        }
        voucherAccoutingService.saveVoucher(voucher);
        for (WarehouseVoucher warehouseVoucher:warehouseVoucherList){
            warehouseVoucher.setVoucher(voucher);
            approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
        }
    }

    public void createAccountingForVoucherTP(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);
        MovementDetail movementDetail =  warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList().get(0);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.SA_VOUCHER_DOCTYPE);
        voucher.setGloss("TRANSFERENCIA DE PRODUCTO, " + movementDetail.getProductItem().getFullName() + " (" + movementDetail.getMovementType() +") " + movementDetail.getQuantity() + " unds");

        System.out.println("===> TRANSFER PRODUCT: " + movementDetail.getProductItem().getFullName() + " " + movementDetail.getQuantity() + " unds");


        if (movementDetail.getMovementType().equals(MovementDetailType.S)){

            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

            VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode(),
                                                            amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                                                            null, null);

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                    BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity());

            voucher.getDetails().add(voucherDetailDebit);
            voucher.getDetails().add(voucherDetailCredit);
            System.out.println("=====> PROCESANDO SALIDA <======");
        }

        if (movementDetail.getMovementType().equals(MovementDetailType.E)){
            System.out.println("=====> PROCESANDO ENTRADA <======");
            Voucher origVoucher = warehouseVoucher.getOrigin().getVoucher();
            //System.out.println("=====> origVoucher: " + origVoucher.getId() + " - " + origVoucher.getDocumentType() + "-" + origVoucher.getDocumentNumber() + " - " + origVoucher.getDetails().size());
            BigDecimal amount = origVoucher.getDetails().get(0).getDebit();

            VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                    amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity());

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode(),
                    BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
                    null, null);

            voucher.getDetails().add(voucherDetailDebit);
            voucher.getDetails().add(voucherDetailCredit);
        }

        /*System.out.println("-----> warehouseVoucher: " + warehouseVoucher.getNumber());
        if (warehouseVoucher.getDestiny() != null)
            System.out.println("======> DESTINO: " + warehouseVoucher.getDestiny());
        if (warehouseVoucher.getOrigin() != null)
            System.out.println("======> ORIGEN: " + warehouseVoucher.getOrigin());*/


        voucherAccoutingService.saveVoucher(voucher);
        warehouseVoucher.setVoucher(voucher);
        approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
    }

    @End
    public String postWarehouseOuput(){
        System.out.println("....Contabilizando Salidas de Almacen....");
        List<WarehouseVoucher> warehouseVoucherList = warehouseVoucherService.findWarehouseVoucherNoAccounting(this.documentType, this.startDate, this.endDate);
        System.out.println("----------> size: " + warehouseVoucherList.size());
        for (WarehouseVoucher warehouseVoucher : warehouseVoucherList){
            System.out.println("------------> wv: " + warehouseVoucher.getDocumentCode() + " - " + warehouseVoucher.getDate() + " - " + warehouseVoucher.getVoucher());
        }

        String outcome = Outcome.FAIL;
        if (warehouseVoucherList.size() > 0)
            outcome = warehouseVoucherService.createWarehouseVoucherListAccounting(warehouseVoucherList);

        if (outcome.equals(Outcome.SUCCESS))
            addCreatedMessage();

        return outcome;
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;
    }
}
