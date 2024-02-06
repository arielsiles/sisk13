package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.production.RawMaterialPaymentService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Name("rawMaterialPaymentAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialPaymentAction extends GenericAction<RawMaterialPayment> {

    private List<RawMaterialPaymentDetail> paymentDetails = new ArrayList<RawMaterialPaymentDetail>();
    private List<CollectMaterial> selectedCollectMaterialItems = new ArrayList<CollectMaterial>();
    private List<RawMaterialDiscount> discounts = new ArrayList<RawMaterialDiscount>();
    private PartialPaymentRawMaterial partialPaymentToRemove = new PartialPaymentRawMaterial();

    private List<PartialPaymentRawMaterial> partialPaymentRawMaterials = new ArrayList<PartialPaymentRawMaterial>();

    private BigDecimal totalWeight   = BigDecimal.ZERO;
    private BigDecimal totalAmount   = BigDecimal.ZERO;
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    private BigDecimal liquidAmount = BigDecimal.ZERO;

    private BigDecimal totalPartialPayment = BigDecimal.ZERO;

    private CollectMaterialState approvedStatus = CollectMaterialState.APR;

    @In
    private RawMaterialPaymentService rawMaterialPaymentService;

    @Factory(value = "rawMaterialPayment", scope = ScopeType.STATELESS)
    public RawMaterialPayment initRawMaterialPayment() {
        return getInstance();
    }

    @Override
    @End
    public String create() {
        try {
            RawMaterialPayment rawMaterialPayment = getInstance();

            getService().create(rawMaterialPayment);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

        setOp(OP_UPDATE);
        return Outcome.SUCCESS;
    }

    @Override
    public String update(){

        RawMaterialPayment rawMaterialPayment = getInstance();
        rawMaterialPayment.setPaymentAmount(totalAmount);
        rawMaterialPayment.setDiscountAmount(totalDiscount);
        rawMaterialPayment.setPartialAmount(totalPartialPayment);
        rawMaterialPayment.setLiquidAmount(liquidAmount);

        rawMaterialPaymentService.saveRawMaterialPayment(rawMaterialPayment, paymentDetails, discounts,partialPaymentRawMaterials);

        return Outcome.SUCCESS;
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(RawMaterialPayment instance) {
        String outCome = super.select(instance);

        System.out.println("---------------SELECT--------------");
        System.out.println("-----> " + instance.getCashBoxCashAccount());
        System.out.println("-----> " + instance.getBankAccount());

        List<RawMaterialPaymentDetail> paymentDetails = rawMaterialPaymentService.getPaymentDetails(instance.getId());
        List<RawMaterialDiscount> discountList = rawMaterialPaymentService.getRawMaterialDiscounts(instance.getId());
        List<PartialPaymentRawMaterial> partialPaymentRawMaterials = rawMaterialPaymentService.getPartialPaymentRawMaterials(instance.getId());

        setPaymentDetails(paymentDetails);
        setDiscounts(discountList);
        setPartialPaymentRawMaterials(partialPaymentRawMaterials);

        loadCollectMaterialItems(paymentDetails);

        updateTotals();

        return outCome;
    }

    @Override
    @End
    public String delete() {
        rawMaterialPaymentService.deleteRawMaterialPayment(getInstance());
        addDeletedMessage();
        return Outcome.SUCCESS;
    }

    public String sendRequest() {

        RawMaterialPayment rawMaterialPayment = getInstance();
        rawMaterialPayment.setState(RawMaterialPaymentState.PAY);

        rawMaterialPayment.setPaymentAmount(totalAmount);
        rawMaterialPayment.setDiscountAmount(totalDiscount);
        rawMaterialPayment.setPartialAmount(totalPartialPayment);
        rawMaterialPayment.setLiquidAmount(liquidAmount);

        rawMaterialPaymentService.saveRawMaterialPayment(rawMaterialPayment, paymentDetails, discounts,partialPaymentRawMaterials);

        return Outcome.SUCCESS;

    }

    public PartialPaymentRawMaterial getPartialPaymentToRemove() {
        return partialPaymentToRemove;
    }

    public void setPartialPaymentToRemove(PartialPaymentRawMaterial partialPaymentToRemove) {
        this.partialPaymentToRemove = partialPaymentToRemove;
    }

    public boolean isPending(){
        return getInstance().getState().equals(RawMaterialPaymentState.PENDING);
    }

    public boolean isSent(){
        return getInstance().getState().equals(RawMaterialPaymentState.PAY);
    }

    public boolean isLiquidated(){
        return getInstance().getState().equals(RawMaterialPaymentState.LIQUIDATE);
    }

    public void loadCollectMaterialItems(List<RawMaterialPaymentDetail> paymentDetails){

        selectedCollectMaterialItems.clear();

        for (RawMaterialPaymentDetail paymentDetail : paymentDetails) {
                selectedCollectMaterialItems.add(paymentDetail.getCollectMaterial());
        }

    }

    public void addCollectMaterialItems(List<CollectMaterial> collectMaterialItems) {

        for (CollectMaterial collectMaterialItem : collectMaterialItems) {

            if (selectedCollectMaterialItems.contains(collectMaterialItem)) {
                continue;
            }

            selectedCollectMaterialItems.add(collectMaterialItem);

            RawMaterialPaymentDetail paymentDetail = new RawMaterialPaymentDetail();
            paymentDetail.setCollectMaterial(collectMaterialItem);
            paymentDetail.setRawMaterialPayment(getInstance());

            this.paymentDetails.add(paymentDetail);
        }

        updateTotals();
    }

    public void addDiscount(){
        RawMaterialDiscount discount = new RawMaterialDiscount();
        discount.setAmount(BigDecimal.ZERO);
        this.discounts.add(discount);
    }

    public void addPartialPayment(){
        PartialPaymentRawMaterial partialPaymentRawMaterial = new PartialPaymentRawMaterial();
        partialPaymentRawMaterial.setDate( new Date());
        partialPaymentRawMaterial.setAmount(BigDecimal.ZERO);
        this.partialPaymentRawMaterials.add(partialPaymentRawMaterial);
    }

    public void removeDiscount(RawMaterialDiscount discount){
        boolean removido = discounts.remove(discount);

        if (removido) {
            System.out.println(" fue removido de la lista.");
        } else {
            System.out.println(" no fue encontrado en la lista.");
        }
        updateTotals();
    }


    public void removePartialPayment(PartialPaymentRawMaterial partialPaymentRawMaterial){
        if(partialPaymentRawMaterial.getId() != null){
            rawMaterialPaymentService.deletePartialPayment(partialPaymentRawMaterial);
            partialPaymentRawMaterials.remove(partialPaymentRawMaterial);
        }else{
            partialPaymentRawMaterials.remove(partialPaymentRawMaterial);
        }
        updateTotals();
    }

    public void updatePartialPaymentTotals(PartialPaymentRawMaterial partialPaymentItem) {
        BigDecimal totalAmountAux   = calculateTotalAmount(paymentDetails);
        BigDecimal totalPartialPaymentAux = calculateTotalPartialPayment();
        BigDecimal totalDiscountAux = calculateTotalDiscounts();
        System.out.println("totalAmountAux: " + BigDecimalUtil.subtract(totalAmountAux, totalDiscountAux,totalPartialPaymentAux));
        System.out.println("resultado: " + BigDecimalUtil.subtract(totalAmountAux, totalDiscountAux,totalPartialPaymentAux).compareTo(BigDecimal.ZERO));
        if(BigDecimalUtil.subtract(totalAmountAux, totalDiscountAux,totalPartialPaymentAux).compareTo(BigDecimal.ZERO) ==  -1){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede liquidar un pago mayor a la deuda(Liquido Pagable).", null));
            //partialPaymentItem.setAmount(liquidAmount);
            partialPaymentItem.setAmount(BigDecimal.ZERO);
            return;
        }
        updateTotals();
    }

    public void updateTotals(){

        Double totalW = 0.0;
        for (RawMaterialPaymentDetail paymentDetail : paymentDetails) {
            totalW = totalW + paymentDetail.getCollectMaterial().getBalanceWeight().doubleValue();
        }

        setTotalWeight(BigDecimalUtil.toBigDecimal(totalW));

        BigDecimal totalAmountAux   = calculateTotalAmount(paymentDetails);
        BigDecimal totalPartialPaymentAux = calculateTotalPartialPayment();
        BigDecimal totalDiscountAux = calculateTotalDiscounts();
        BigDecimal liquidAmountAux  = BigDecimalUtil.subtract(totalAmountAux, totalDiscountAux,totalPartialPaymentAux);

        setTotalAmount(totalAmountAux);
        setTotalPartialPayment(totalPartialPaymentAux);
        setTotalDiscount(totalDiscountAux);
        setLiquidAmount(liquidAmountAux);
    }

    public BigDecimal calculateTotalAmount(List<RawMaterialPaymentDetail> paymentDetails){
        BigDecimal result = BigDecimal.ZERO;

        for (RawMaterialPaymentDetail rawMaterialPaymentDetail : paymentDetails){
            CollectMaterial mp = rawMaterialPaymentDetail.getCollectMaterial();

            BigDecimal tonWeight = BigDecimalUtil.divide(mp.getBalanceWeight(), BigDecimalUtil.toBigDecimal(1000));
            BigDecimal priceTons = BigDecimalUtil.multiply(tonWeight, mp.getPrice());
            result = BigDecimalUtil.sum(result, priceTons);
        }
        return result;
    }

    public BigDecimal calculateTotalDiscounts(){

        BigDecimal result = BigDecimal.ZERO;

        for (RawMaterialDiscount discount : this.discounts) {
            result = BigDecimalUtil.sum(result, discount.getAmount());
        }
        return result;
    }

    public BigDecimal calculateTotalPartialPayment(){

        BigDecimal result = BigDecimal.ZERO;

        for (PartialPaymentRawMaterial paymentDetail : this.partialPaymentRawMaterials) {
            result = BigDecimalUtil.sum(result, paymentDetail.getAmount());
        }
        return result;
    }

    public void assignBeneficiary(RawMaterialProducer rawMaterialProducerItem){
        getInstance().setProducer(rawMaterialProducerItem);
    }

    public void clearRawMaterialProducer() {
        getInstance().setProducer(null);
    }


    public void paymentTypeChanged() {
        if ( isBankPayment() ) {
            getInstance().setCashBoxCashAccount(null);
        } else if (isCashBoxPayment()) {
            getInstance().setBankAccount(null);
        }
    }

    public boolean isBankPayment() {
        return getInstance().getPaymentType() != null &&
                getInstance().getPaymentType().equals(RawMaterialPaymentType.PAYMENT_BANK_ACCOUNT);
    }

    public boolean isCashBoxPayment() {
        return null != getInstance().getPaymentType()
                && getInstance().getPaymentType().equals(RawMaterialPaymentType.PAYMENT_CASHBOX);
    }

    public boolean isEnableBankAccount() {
        return isBankPayment();
    }

    public void assignCashBoxCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = genericService.findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundErrorLog(e);
        }
        //getLiquidationPayment().setCashBoxCashAccount(cashAccount);
        System.out.println("---------------> cashAccount: " + cashAccount);
        getInstance().setCashBoxCashAccount(cashAccount);
        //accountChanged();
    }

    public String liquidatePayment(){

        RawMaterialPayment rawMaterialPayment = getInstance();
        rawMaterialPayment.setState(RawMaterialPaymentState.LIQUIDATE);
        rawMaterialPayment.setApprovalDate(new Date());
        rawMaterialPaymentService.saveLiquidateRawMaterialPayment(rawMaterialPayment);

        addLiquidatedMessage();
        return Outcome.SUCCESS;
    }

    public List<RawMaterialPaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<RawMaterialPaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public List<CollectMaterial> getSelectedCollectMaterialItems() {
        return selectedCollectMaterialItems;
    }

    public void setSelectedCollectMaterialItems(List<CollectMaterial> selectedCollectMaterialItems) {
        this.selectedCollectMaterialItems = selectedCollectMaterialItems;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public CollectMaterialState getApprovedStatus() {
        return approvedStatus;
    }

    public void setApprovedStatus(CollectMaterialState approvedStatus) {
        this.approvedStatus = approvedStatus;
    }

    protected void addLiquidatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.liquidated", getDisplayPropertyValue());
    }

    public List<RawMaterialDiscount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<RawMaterialDiscount> discounts) {
        this.discounts = discounts;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getLiquidAmount() {
        return liquidAmount;
    }

    public void setLiquidAmount(BigDecimal liquidAmount) {
        this.liquidAmount = liquidAmount;
    }

    public List<PartialPaymentRawMaterial> getPartialPaymentRawMaterials() {
        return partialPaymentRawMaterials;
    }

    public void setPartialPaymentRawMaterials(List<PartialPaymentRawMaterial> partialPaymentRawMaterials) {
        this.partialPaymentRawMaterials = partialPaymentRawMaterials;
    }

    public BigDecimal getTotalPartialPayment() {
        return totalPartialPayment;
    }

    public void setTotalPartialPayment(BigDecimal totalPartialPayment) {
        this.totalPartialPayment = totalPartialPayment;
    }

    public RawMaterialPaymentService getRawMaterialPaymentService() {
        return rawMaterialPaymentService;
    }

    public void setRawMaterialPaymentService(RawMaterialPaymentService rawMaterialPaymentService) {
        this.rawMaterialPaymentService = rawMaterialPaymentService;
    }
}
