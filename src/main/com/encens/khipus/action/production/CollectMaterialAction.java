package com.encens.khipus.action.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.service.production.CollectMaterialService;
import com.encens.khipus.service.production.ProducerPriceService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

@Name("collectMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class CollectMaterialAction extends GenericAction<CollectMaterial> {

    private RawMaterialProducer rawMaterialProducer;
    private ProductiveZone productiveZone;
    private BigDecimal rawMaterialPrice = BigDecimal.ZERO;

    private Date startDate = new Date();
    private Date endDate  = new Date();

    /** Datos del modal de reversion, cacheados por conversacion. */
    private Voucher voucher;
    private BigDecimal currentBalance;
    private BigDecimal currentUnitCost;
    private BigDecimal projectedUnitCost;
    private int revertWindowDays;
    private boolean revertDataLoaded = false;
    private String revertReason;

    @In
    private ProducerPriceService producerPriceService;

    @In
    private CollectMaterialService collectMaterialService;

    @In
    private InventoryService inventoryService;

    @Factory(value = "collectMaterial", scope = ScopeType.STATELESS)
    public CollectMaterial initCollectMaterial() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }

    @Override
    public String create() {

        try {
            CollectMaterial collectMaterial = getInstance();

            getService().create(collectMaterial);
            setOp(OP_UPDATE);
            setInstance(collectMaterial);
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    @Override
    public String update() {

        CollectMaterial collectMaterial = getInstance();

        String outcome = super.update();
        setOp(OP_UPDATE);
        //setInstance(collectMaterial);

        return outcome;
    }

    public String update(CollectMaterial collectMaterial) {
        Long currentVersion = (Long) getVersion(collectMaterial);
        try {
            getService().update(collectMaterial);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        //addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @End
    public String approve(){

        try {

            CollectMaterial collectMaterial = getInstance();
            collectMaterial.setState(CollectMaterialState.APR);
            getService().update(collectMaterial);

            inventoryService.updateInventoryForCollectMaterial(collectMaterial);

            addApprovedMessage();

            return Outcome.SUCCESS;

        } catch (EntryDuplicatedException e) {
            e.printStackTrace();
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            e.printStackTrace();
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    public String accounting(){

        List<CollectMaterial> collectMaterialList = collectMaterialService.findCollectMaterialNoAccounting( this.startDate, this.endDate);
        String outcome = Outcome.FAIL;
        if (collectMaterialList.size() > 0) {
            outcome = collectMaterialService.createCollectMaterialListAccounting(collectMaterialList,startDate,endDate);
            for( CollectMaterial collectMaterial:collectMaterialList){
                collectMaterial.setState(CollectMaterialState.CONTA);
                update(collectMaterial);
            }
        }else{
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "No se encontraron registros para contabilizar");
        }
        if (outcome.equals(Outcome.SUCCESS))
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "se contabilizó correctamente");

        return outcome;
    }

    public void updateProducerPrice(){
        CollectMaterial instance = getInstance();
        //setRawMaterialPrice(instance.getMetaProduct().getPrice());
        instance.setPrice(instance.getMetaProduct().getPrice());

        BigDecimal price = producerPriceService.findProducerPrice(instance.getMetaProduct(), instance.getProducer());
        if ( price.doubleValue() > 0 ) {
            //setRawMaterialPrice(price);
            instance.setPrice(price);
        }
    }

    public void assignSupplier(RawMaterialProducer supplier){
        setRawMaterialProducer(supplier);
        getInstance().setProducer(supplier);
    }

    public void assignOrigin(ProductiveZone productiveZone){
        setProductiveZone(productiveZone);
        getInstance().setProductiveZone(productiveZone);
    }

    public void clearRawMaterialProducer() {
        getInstance().setProducer(null);
    }

    public void clearOrigin() {
        getInstance().setProductiveZone(null);
    }

    public void assignEmployee(Employee employee) {
        getInstance().setReceptionEmployee(employee);
    }

    public boolean isPending(){
        return getInstance().getState().equals(CollectMaterialState.PEN);
    }

    /* ------------------------------------------------------------------------
     * Revertir acopio (Aprobado / Contabilizado -> Pendiente)
     * ------------------------------------------------------------------------ */

    public boolean isApproved(){
        return getInstance().getState().equals(CollectMaterialState.APR);
    }

    public boolean isAccounted(){
        return getInstance().getState().equals(CollectMaterialState.CONTA);
    }

    /** El boton se ofrece en estos dos estados; si esta bloqueado, el modal explica por que. */
    public boolean isRevertVisible(){
        return isApproved() || isAccounted();
    }

    /** Contabilizado con asiento aun vigente: hay que anularlo primero. */
    public boolean isBlockedByVoucher(){
        Voucher voucher = getVoucher();
        return isAccounted() && voucher != null && !voucher.isNullified();
    }

    /** Contabilizado sin vinculo al asiento: lo contabilizado con el esquema viejo ('IA',
     *  un asiento por dia para varios acopios). No se toca. */
    public boolean isBlockedByMissingLink(){
        return isAccounted() && getInstance().getVoucherId() == null;
    }

    /** Fuera de la ventana de reversion configurada. */
    public boolean isBlockedByWindow(){
        int days = getRevertWindowDays();
        return days > 0 && getInstance().getDate() != null
                && DateUtils.daysBetween(getInstance().getDate(), new Date(), false) > days;
    }

    /** El material ya se consumio: no hay saldo para descontar. */
    public boolean isBlockedByStock(){
        return getCurrentBalance().compareTo(getInstance().getBalanceWeight()) < 0;
    }

    public boolean isRevertBlocked(){
        return isBlockedByVoucher() || isBlockedByMissingLink() || isBlockedByWindow() || isBlockedByStock();
    }

    /** Mensaje de la guarda que bloquea, ya interpolado. Se arma aca y no en la vista porque
     *  f:param es un UIParameter y no admite converters anidados. */
    public String getRevertBlockedMessage(){
        if (isBlockedByVoucher()) {
            return format("CollectMaterial.revert.blocked.voucher", getVoucherNumber());
        }
        if (isBlockedByMissingLink()) {
            return Messages.instance().get("CollectMaterial.revert.blocked.missingLink");
        }
        if (isBlockedByWindow()) {
            return format("CollectMaterial.revert.blocked.window",
                    String.valueOf(getRevertWindowDays()),
                    DateUtils.format(getInstance().getDate(), Messages.instance().get("patterns.date")));
        }
        if (isBlockedByStock()) {
            return format("CollectMaterial.revert.blocked.stock",
                    decimal(getInstance().getBalanceWeight()), decimal(getCurrentBalance()));
        }
        return "";
    }

    private String format(String key, Object... params) {
        return MessageFormat.format(Messages.instance().get(key), params);
    }

    private String decimal(BigDecimal value) {
        return new DecimalFormat(Messages.instance().get("patterns.decimalNumber")).format(value);
    }

    public boolean isRevertable(){
        return isRevertVisible() && !isRevertBlocked();
    }

    /** Todo lo del modal se calcula una vez por conversacion: cada entrada a la pantalla
     *  desde el listado abre una conversacion nueva, asi que siempre son datos frescos. */
    public Voucher getVoucher(){
        if (!revertDataLoaded) {
            voucher            = collectMaterialService.findVoucher(getInstance());
            currentBalance     = collectMaterialService.findCurrentBalance(getInstance());
            currentUnitCost    = collectMaterialService.findCurrentUnitCost(getInstance());
            projectedUnitCost  = collectMaterialService.findProjectedUnitCost(getInstance());
            revertWindowDays   = collectMaterialService.getRevertWindowDays();
            revertDataLoaded   = true;
        }
        return voucher;
    }

    /** Numero tal como lo ve el contador: tipo + no_doc, el correlativo por tipo de documento
     *  que muestra y filtra la pantalla de Comprobantes. NO se usa no_trans, que es una
     *  secuencia global interna y no aparece en ninguna pantalla. */
    public String getVoucherNumber(){
        Voucher voucher = getVoucher();
        if (voucher == null) {
            return "";
        }
        String number = voucher.getDocumentNumber() != null ? voucher.getDocumentNumber()
                                                            : voucher.getTransactionNumber();
        return voucher.getDocumentType() + "-" + number;
    }

    public BigDecimal getCurrentBalance(){
        getVoucher();
        return currentBalance == null ? BigDecimal.ZERO : currentBalance;
    }

    public BigDecimal getProjectedBalance(){
        return BigDecimalUtil.subtract(getCurrentBalance(), getInstance().getBalanceWeight());
    }

    public BigDecimal getCurrentUnitCost(){
        getVoucher();
        return currentUnitCost == null ? BigDecimal.ZERO : currentUnitCost;
    }

    public BigDecimal getProjectedUnitCost(){
        getVoucher();
        return projectedUnitCost == null ? BigDecimal.ZERO : projectedUnitCost;
    }

    public int getRevertWindowDays(){
        getVoucher();
        return revertWindowDays;
    }

    public String getRevertReason() {
        return revertReason;
    }

    public void setRevertReason(String revertReason) {
        this.revertReason = revertReason;
    }

    /** Revierte el acopio. Revalida las guardas en servidor: el estado pudo cambiar entre
     *  que se pinto la pantalla y que se apreto el boton.
     *  Sin @End: si algo falla hay que quedarse en la pantalla con el mensaje. En el camino
     *  feliz la conversacion la cierra el <end-conversation/> del listado. */
    public String revert(){

        CollectMaterial collectMaterial = getInstance();

        if (!isRevertVisible() || isRevertBlocked()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CollectMaterial.revert.notAllowed");
            return Outcome.REDISPLAY;
        }

        if (revertReason == null || revertReason.trim().length() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CollectMaterial.revert.reasonRequired");
            return Outcome.REDISPLAY;
        }

        collectMaterialService.revert(collectMaterial, revertReason.trim());

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "CollectMaterial.revert.done", getDisplayPropertyValue());

        return Outcome.SUCCESS;
    }

    public BigDecimal getRawMaterialPrice() {
        return rawMaterialPrice;
    }

    public void setRawMaterialPrice(BigDecimal rawMaterialPrice) {
        this.rawMaterialPrice = rawMaterialPrice;
    }

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    protected void addApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.approved", getDisplayPropertyValue());
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
