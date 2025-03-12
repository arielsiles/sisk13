package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.ProductionProcess;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
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
                " and w.date between :startDate and :endDate " +
                " and w.state =:state")
                .setParameter("documentType", documentType)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("state", WarehouseVoucherState.APR)
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

                    /*
                    CashAccount expenseCashAccount = movementDetail.getProductItem().getSubGroup().getGroup().getCostCashAccount();
                    if (warehouseVoucher.getExpenseType().equals(ExpenseType.ADMINISTRATIVE)){
                        expenseCashAccount = movementDetail.getProductItem().getSubGroup().getGroup().getExpenseCashAccount();
                    }
                    */
                    CashAccount expenseCashAccount = getCashAccountExpense(warehouseVoucher, movementDetail);

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
            String voucherDate = DateUtils.format(warehouseVoucher.getDate(), "dd/MM/yyyy");
            gloss = gloss + " " + voucherDate + ", novale: " + warehouseVoucher.getNumber();
            voucher.setGloss(gloss);
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
        voucher.setState(VoucherState.APR.toString());
        voucherAccoutingService.saveVoucher(voucher);

        for (WarehouseVoucher warehouseVoucher:warehouseVoucherList){
            warehouseVoucher.setVoucher(voucher);
            em.merge(warehouseVoucher);
        }
        em.flush();

        result = Outcome.SUCCESS;
        return  result;
    }

    @Override
    public WarehouseVoucher findWarehouseVoucherByPurchaseOrder(PurchaseOrder purchaseOrder) {

        WarehouseVoucher result = (WarehouseVoucher) em.createQuery("select w from WarehouseVoucher w " +
                        " where w.purchaseOrderId =:purchaseOrderId ")
                        .setParameter("purchaseOrderId", purchaseOrder.getId())
                        .getSingleResult();

        return result;
    }

    @Override
    public List<ProductionProcess> getProductionProcesses(ProductionLine productionLine) {

            List<ProductionProcess> resultList = em.createQuery("select p from ProductionProcess p" +
                    " where p.productionLine =:productionLine" +
                    " order by p.position")
                    .setParameter("productionLine", productionLine)
                    .getResultList();

            return resultList;
    }

    @Override
    public List<ProductItem> getFinishedGoodsList() {

        List<ProductItem> resultList = em.createQuery("select p from ProductItem p" +
                " where p.warehouse.warehouseType =:warehouseType")
                .setParameter("warehouseType", WarehouseType.FINISHED_GOODS)
                .getResultList();

        return resultList;
    }

    @Override
    public List<AnalyticDetail> getAnalyticDetails(Destination destination) {

            List<AnalyticDetail> resultList = em.createQuery("select a from AnalyticDetail a" +
                    " where a.destination =:destination")
                    .setParameter("destination", destination)
                    .getResultList();

            return resultList;
    }

    /**
     *
     * @param warehouseVoucher
     * @param movementDetail
     * @return Retorna la cuenta de costo o gasto asignada en grupos, si es null, retorna la cuenta asignada en el articulo
     */
    private CashAccount getCashAccountExpense(WarehouseVoucher warehouseVoucher, MovementDetail movementDetail) {

        if (movementDetail.getProductItem().getSubGroup().getGroup().getCostCashAccount() == null){
            return movementDetail.getProductItem().getCashAccount();
        }

        CashAccount expenseCashAccount = movementDetail.getProductItem().getSubGroup().getGroup().getCostCashAccount();

        /** Anterior, seleccionaba cuenta de gasto o costo de la configuracion de inv_grupos **/
        /*if (warehouseVoucher.getExpenseType().equals(ExpenseType.ADMINISTRATIVE)){
            if (movementDetail.getProductItem().getSubGroup().getGroup().getExpenseCashAccount() != null) {
                expenseCashAccount = movementDetail.getProductItem().getSubGroup().getGroup().getExpenseCashAccount();
            }else{
                expenseCashAccount = movementDetail.getProductItem().getCashAccount();
            }
        }*/

        if (warehouseVoucher.getExpenseCashAccount() != null){
                expenseCashAccount = warehouseVoucher.getExpenseCashAccount();
        }

        return expenseCashAccount;
    }

}
