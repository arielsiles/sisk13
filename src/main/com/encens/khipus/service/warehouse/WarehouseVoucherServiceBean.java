package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.warehouse.InventoryMovement;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.VoucherBuilder;
import com.encens.khipus.util.VoucherDetailBuilder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;

@Stateless
@Name("warehouseVoucherService")
@AutoCreate
public class WarehouseVoucherServiceBean implements WarehouseVoucherService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @Override
    public List<WarehouseVoucher> findWarehouseVoucherNoAccounting(WarehouseDocumentType documentType, Date startDate, Date endDate) {

        List<WarehouseVoucher> resultList =
        em.createQuery("select w from WarehouseVoucher w " +
                " where w.documentType =:documentType " +
                " and w.voucher is null " +
                " and w.operation is null" +
                " and w.date between :startDate and :endDate ")
                .setParameter("documentType", documentType)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        return resultList;
    }

    @Override
    public String createWarehouseVoucherListAccounting(List<WarehouseVoucher> warehouseVoucherList) {
        String result = Outcome.FAIL;
        String gloss = "CONTABILIZANDO SALIDAS DIARIAS DE ALMACEN";

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
        voucher.setDocumentType(Constants.SA_VOUCHER_DOCTYPE);

        List<VoucherDetail> warehouseVoucherDetailCashAcounts = new ArrayList<VoucherDetail>();

        for (WarehouseVoucher warehouseVoucher:warehouseVoucherList){
            for (InventoryMovement inventoryMovement:warehouseVoucher.getInventoryMovementList()){
                for (MovementDetail movementDetail:inventoryMovement.getMovementDetailList()){
                    CashAccount expenseCashAccount = movementDetail.getProductItem().getCashAccount();
                    VoucherDetail voucherDetail = VoucherDetailBuilder.newDebitVoucherDetail(
                            null, null, expenseCashAccount, movementDetail.getAmount(), FinancesCurrencyType.P, BigDecimal.ONE);
                    voucher.getDetails().add(voucherDetail);

                    CashAccount warehouseCashAccount = warehouseVoucher.getWarehouse().getWarehouseCashAccount();
                    VoucherDetail warehouseCashAccountOutput = VoucherDetailBuilder.newCreditVoucherDetail(
                            null, null, warehouseCashAccount, movementDetail.getAmount(), FinancesCurrencyType.P, BigDecimal.ONE);
                    warehouseCashAccountOutput.setProductItemCode(movementDetail.getProductItemCode());
                    warehouseCashAccountOutput.setQuantityArt(movementDetail.getQuantity());
                    warehouseVoucherDetailCashAcounts.add(warehouseCashAccountOutput);
                }
            }
        }

        Collections.sort(warehouseVoucherDetailCashAcounts, new Comparator<VoucherDetail>() {
            @Override
            public int compare(VoucherDetail o1, VoucherDetail o2) {
                return o1.getAccount().compareTo(o2.getAccount());
            }
        });

        for (VoucherDetail voucherDetail:warehouseVoucherDetailCashAcounts){
            voucher.getDetails().add(voucherDetail);
        }

        voucher.setDate(warehouseVoucherList.get(0).getDate()); /** Temporal **/
        voucherAccoutingService.saveVoucher(voucher);

        for (WarehouseVoucher warehouseVoucher:warehouseVoucherList){
            warehouseVoucher.setVoucher(voucher);
            em.merge(warehouseVoucher);
        }
        em.flush();

        result = Outcome.SUCCESS;
        return  result;
    }
}
