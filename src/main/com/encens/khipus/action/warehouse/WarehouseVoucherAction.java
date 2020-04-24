package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
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
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

        for (WarehouseVoucher warehouseVoucher : warehouseVoucherList){

            if (warehouseVoucher.getOperation().equals(VoucherOperation.TP)){
                createAccountingForVoucherTP(warehouseVoucher, startDate, endDate);
            }

            if (warehouseVoucher.getOperation().equals(VoucherOperation.BA)){
                createAccountingForVoucherBA(warehouseVoucher, startDate, endDate);
            }

            if (warehouseVoucher.getOperation().equals(VoucherOperation.DE)){
                createAccountingForVoucherDE(warehouseVoucher, startDate, endDate);
            }

        }
    }


    public void createAccountingForVoucherBA(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.TR_VOUCHER_DOCTYPE);
        voucher.setGloss("BAJA DE PRODUCTO, " + warehouseVoucher.getInventoryMovementList().get(0).getDescription());

        VoucherDetail voucherDetailDebit = new VoucherDetail("4460111100", BigDecimal.ZERO, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,null, null);
        voucher.getDetails().add(voucherDetailDebit);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
            System.out.println("=====> BAJA: " + movementDetail.getProductItem().getFullName());
            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
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
        voucher.setDocumentType(Constants.TR_VOUCHER_DOCTYPE);
        voucher.setGloss("DEVOLUCION/ENTRADA PT, " + warehouseVoucher.getInventoryMovementList().get(0).getDescription());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
            System.out.println("------> VOUCHER DEV: " + movementDetail.getProductItem().getFullName());
            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            System.out.println("------> unitCost: " + unitCost);
            System.out.println("------> Quantity: " + movementDetail.getQuantity());
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

            VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                    amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity());

            voucher.getDetails().add(voucherDetailDebit);
            totalAmount = BigDecimalUtil.sum(totalAmount, amount, 2);
        }

        VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaCostPT().getAccountCode(),
                BigDecimal.ZERO, totalAmount, FinancesCurrencyType.P, BigDecimal.ONE,null, null);
        voucher.getDetails().add(voucherDetailCredit);

        voucherAccoutingService.saveVoucher(voucher);
        warehouseVoucher.setVoucher(voucher);
        approvalWarehouseVoucherService.updateSimpleWarehouseVoucher(warehouseVoucher);
    }


    public void createAccountingForVoucherTP(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = voucherAccoutingService.getUnitCost_milkProducts(startDate, endDate);
        MovementDetail movementDetail =  warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList().get(0);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.TR_VOUCHER_DOCTYPE);
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
