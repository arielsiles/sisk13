package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.warehouse.WarehouseAccountEntryService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

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

    private Date startDate;
    private Date endDate;

    @In
    private WarehouseAccountEntryService warehouseAccountEntryService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    @Factory(value = "warehouseVoucher", scope = ScopeType.STATELESS)
    public WarehouseVoucher initWarehouseVoucher() {
        return getInstance();
    }

    @Factory(value = "warehouseVoucherStates", scope = ScopeType.STATELESS)
    public WarehouseVoucherState[] getWarehouseVoucherStates() {
        return WarehouseVoucherState.values();
    }



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
        //MovementDetail movementDetail =  warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList().get(0);

        Voucher voucher = new Voucher();
        voucher.setDate(endDate);
        voucher.setDocumentType(Constants.TR_VOUCHER_DOCTYPE);
        voucher.setGloss("BAJA DE PRODUCTO, " + warehouseVoucher.getInventoryMovementList().get(0).getDescription());

        VoucherDetail voucherDetailDebit = new VoucherDetail("4460111100", BigDecimal.ZERO, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,null, null);
        voucher.getDetails().add(voucherDetailDebit);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (MovementDetail movementDetail : warehouseVoucher.getInventoryMovementList().get(0).getMovementDetailList()){
            BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
            BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
            BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
            movementDetail.getProductItemCode(), movementDetail.getQuantity().longValue());

            voucher.getDetails().add(voucherDetailCredit);
            totalAmount = BigDecimalUtil.sum(totalAmount, amount, 2);
        }

        voucherDetailDebit.setDebit(totalAmount);
        voucherAccoutingService.saveVoucher(voucher);
    }

    public void createAccountingForVoucherDE(WarehouseVoucher warehouseVoucher, Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

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

        BigDecimal unitCost = unitCostMilkProducts.get(movementDetail.getProductItemCode());
        BigDecimal amount = BigDecimalUtil.multiply(movementDetail.getQuantity(), unitCost, 2);

        if (movementDetail.getMovementType().equals(MovementDetailType.S)){

            VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode(),
                                                            amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                                                            null, null);

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                    BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity().longValue());

            voucher.getDetails().add(voucherDetailDebit);
            voucher.getDetails().add(voucherDetailCredit);
        }

        if (movementDetail.getMovementType().equals(MovementDetailType.E)){
            VoucherDetail voucherDetailDebit = new VoucherDetail(companyConfiguration.getCtaAlmPT().getAccountCode(),
                    amount, BigDecimal.ZERO, FinancesCurrencyType.P, BigDecimal.ONE,
                    movementDetail.getProductItemCode(), movementDetail.getQuantity().longValue());

            VoucherDetail voucherDetailCredit = new VoucherDetail(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode(),
                    BigDecimal.ZERO, amount, FinancesCurrencyType.P, BigDecimal.ONE,
                    null, null);

            voucher.getDetails().add(voucherDetailDebit);
            voucher.getDetails().add(voucherDetailCredit);
        }

        voucherAccoutingService.saveVoucher(voucher);
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
}
