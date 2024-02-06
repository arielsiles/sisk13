package com.encens.khipus.service.xproduction;


import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.RawMaterialPayRollException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.*;

import javax.ejb.Local;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Local
public interface RawMaterialPayRollService extends GenericService {

    public DiscountProducer findDiscountProducerByDate(Date date);

    public List<DiscountProducer> findDiscountsProducerByDate(Date date);

    public RawMaterialPayRoll generatePayroll(RawMaterialPayRoll rawMaterialPayRoll,DiscountProducer discountProducer) throws EntryNotFoundException, RawMaterialPayRollException;

    void calculateLiquidPayable(RawMaterialPayRoll rawMaterialPayRoll);

    void create(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException;

    void createAll(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException;

    void validate(RawMaterialPayRoll rawMaterialPayRoll) throws RawMaterialPayRollException;

    RawMaterialPayRoll getTotalsRawMaterialPayRoll(Date dateIni, Date dateEnd, ProductiveZone productiveZone, MetaProduct metaProduct);

    RawMaterialPayRollServiceBean.Discounts getDiscounts(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct);

    RawMaterialPayRollServiceBean.SummaryTotal getSumaryTotal(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct);

    List<RawMaterialPayRecordDetailDummy> generateDetails(RawMaterialPayRecord rawMaterialPayRecord) throws RawMaterialPayRollException;

    Double getTotalWeightMoney(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    Double getTotalMoneyDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    Double getBalanceWeightTotal(Double unitPrice, Date dateIni, Date dateEnd, MetaProduct metaProduct);

    Double getTotalDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    List<RawMaterialPayRoll> findAll(Date startDate, Date endDate, MetaProduct metaProduct);

    List<RawMaterialPayRoll> findAll();

    boolean verifDayColected(Calendar date_aux, ProductiveZone zone);

    void approvedSession(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    public List<RawMaterialPayRoll> findAllPayRollesByGAB(Date startDate, Date endDate, ProductiveZone productiveZone);

    public void approvedNoteRejection(Calendar startDate, Calendar endDate);

    void approvedDiscounts(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    void approvedDiscountsGAB(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    void approvedRawMaterialPayRoll(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    Double getReservProducer(Date startDate, Date endDate);

    void deleteReserveDiscount(Date startDate, Date endDate);

    List<DiscountProducer> findDiscountProducerByDate(Date startDate, Date endDate);

    void approvedReservProductor(Calendar startDate, Calendar endDate);

    public List<BoletaPagoProductor> findBoletaDePago(Date fechaIni, Date fechaFin, RawMaterialProducer rawMaterialProducer, ProductiveZone productiveZone, MetaProduct metaProduct);

    public List<BoletaPagoProductor> findBoletaDePagoGA(Date fechaIni, Date fechaFin, RawMaterialProducer rawMaterialProducer, ProductiveZone productiveZone, MetaProduct metaProduct);
}
