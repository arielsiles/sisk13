package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Creates integration vouchers in finances system
 *
 * @author
 * @version 1.0
 */

@Name("voucherService")
@Stateless
@AutoCreate
@FinancesUser
public class VoucherServiceBean implements VoucherService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private User currentUser;

    @In
    private CompanyConfigurationService companyConfigurationService;

    /**
     * Creates the voucher, then gets the transaction number, sets it to the detail and persist the detail list
     *
     * @param voucher the voucher to persist
     */
    public void create(Voucher voucher) {
        List<VoucherDetail> voucherDetailList = voucher.getDetails();
        voucher = createBody(voucher);
        createDetail(voucher, voucherDetailList);
    }

    /**
     * Creates the voucher body, then gets the transaction number
     *
     * @param voucher the voucher to persist
     */
    public Voucher createBody(Voucher voucher) {
        if (ValidatorUtil.isBlankOrNull(voucher.getUserNumber())) {
            voucher.setUserNumber(currentUser.getFinancesCode());
        }
        voucher.setPendantRegistry("SI");
        if (ValidatorUtil.isBlankOrNull(voucher.getTransactionNumber())) {
            em.persist(voucher);
        } else {
            voucher = em.merge(voucher);
        }
        em.flush();
        return voucher;
    }

    /**
     * Creates the voucher detail
     *
     * @param voucher           The voucher for change if is necessary
     * @param voucherDetailList The detail list that will be persisted
     */
    public void createDetail(Voucher voucher, List<VoucherDetail> voucherDetailList) {
        boolean isEmpty = true;
        for (VoucherDetail detail : voucherDetailList) {
            if (!BigDecimalUtil.isZeroOrNull(detail.getDebit()) || !BigDecimalUtil.isZeroOrNull(detail.getCredit())) {
                isEmpty = false;
                detail.setTransactionNumber(voucher.getTransactionNumber());
                detail.setVoucher(voucher);
                em.persist(detail);
            }
        }
        em.flush();

        if (isEmpty) {
            /*todo fixed using enumeration, this change must be updated in everywhere */
            voucher.setState("ANL");
            em.merge(voucher);
            em.flush();
        }
    }

    public void deleteVoucher(Voucher voucher) {
        em.remove(voucher);
        em.flush();
    }

    public void approvedAllVoucherEntries(String defaultCompanyNumber,
                                          String businessUnit,
                                          Date startDate,
                                          Date endDate,
                                          String numberTransction,
                                          String financeUser,
                                          String financesModule) throws CompanyConfigurationNotFoundException {
        if(numberTransction.isEmpty())
            numberTransction = "%";
         //wise.aprobar_asientos.gen_compro
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        em.createNativeQuery("CALL WISE.APROBAR_ASIENTOS.GEN_COMPRO( " +
                ":financesModule, \n" + //MODULO
                ":cia,\n" + //CIA
                ":businessUnit,\n" + //UNIDAD EJECUTORA
                "to_char(:startDate,'dd-mm-yyyy'), \n" + //FECHA DESDE
                "to_char(:endDate,'dd-mm-yyyy'), \n" + //FECHA HASTA
                ":financeUser, \n" + //USUARIO Q REALIZA LA APROBACION
                ":numberTransction \n" + //TODAS LAS TRANSACCIONES
                ")")
                .setParameter("financesModule", financesModule)
                .setParameter("cia", companyConfiguration.getCompanyNumber())
                .setParameter("businessUnit", businessUnit)
                .setParameter("startDate", startDate,TemporalType.DATE)
                .setParameter("endDate", endDate,TemporalType.DATE)
                .setParameter("financeUser",financeUser)
                .setParameter("numberTransction",numberTransction)
                .executeUpdate();

    }

    @Override
    public DocType getDocType(String name) {

        DocType docType = (DocType)em.createNamedQuery("DocType.findDocumentByName").setParameter("name", name).getSingleResult();

        return docType;
    }

    public void approvedAllVoucherEntries(String defaultCompanyNumber,
                                          BusinessUnit businessUnit,
                                          Date startDate,
                                          Date endDate,
                                          String numberTransction,
                                          FinanceUser financeUser,
                                          FinancesModule financesModule) throws CompanyConfigurationNotFoundException {
        if(numberTransction.isEmpty())
            numberTransction = "%";

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
          em.createNativeQuery("call wise.aprobar_asientos.gen_compro( " +
                              ":financesModule, \n" + //MODULO
                              ":cia,\n" + //CIA
                              ":businessUnit,\n" + //UNIDAD EJECUTORA
                              "to_char(:startDate,'dd-mm-yyyy'), \n" + //FECHA DESDE
                              "to_char(:endDate,'dd-mm-yyyy'), \n" + //FECHA HASTA
                              ":financeUser, \n" + //USUARIO Q REALIZA LA APROBACION
                              ":numberTransction \n" + //TODAS LAS TRANSACCIONES
                              ")")
                  .setParameter("financesModule", financesModule.getId().getModule())
                  .setParameter("cia", companyConfiguration.getCompanyNumber())
                  .setParameter("businessUnit", businessUnit.getExecutorUnitCode())
                  .setParameter("startDate", startDate,TemporalType.DATE)
                  .setParameter("endDate", endDate,TemporalType.DATE)
                  .setParameter("financeUser",financeUser.getId())
                  .setParameter("numberTransction",numberTransction)
                  .executeUpdate();

    }

    private String toString(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return ((calendar.get(Calendar.DAY_OF_MONTH)) > 9 ? (calendar.get(Calendar.DAY_OF_MONTH)) :"0"+(calendar.get(Calendar.DAY_OF_MONTH))) +
                "-" + (((calendar.get(Calendar.MONTH))+1) >9 ? (((calendar.get(Calendar.MONTH))+1)) :"0"+((calendar.get(Calendar.MONTH))+1))  +
                "-" +calendar.get(Calendar.YEAR);
    }

    @Override
    public List<ObsApprovedEntries> getInfoTrasaction(FinancesModule financesModule, Date startDate, Date endDate) {
        List<ObsApprovedEntries> entries = new ArrayList<ObsApprovedEntries>();
                List<Object[]> datas = em.createNativeQuery("SELECT * FROM WISE.OBS_APROBACION_ASIENTOS\n" +
                                                                "WHERE  TRUNC(FECHA) between :startDate and :endDate \n" +
                                                                "AND MODULO  =  :financesModule \n" +
                                                                "AND ESTADO <> 'APROBADO'"
                                                                //"AND NO_TRANS = 0"
                                                                )
                                            .setParameter("financesModule", financesModule.getId().getModule())
                                            .setParameter("startDate",startDate,TemporalType.DATE)
                                            .setParameter("endDate",endDate,TemporalType.DATE)
                                            .getResultList();
        for(Object[] data:datas)
        {
            entries.add(new ObsApprovedEntries((String)data[2],(String)data[6]));
        }

        return entries;

    }

    public List<ObsApprovedEntries> getInfoTrasaction(String financesModule, String numberTransaction, Date startDate, Date endDate) {
        List<ObsApprovedEntries> entries = new ArrayList<ObsApprovedEntries>();
        List<Object[]> datas = em.createNativeQuery("SELECT * FROM WISE.OBS_APROBACION_ASIENTOS \n" +
                        "WHERE  TRUNC(FECHA) between :startDate and :endDate \n" +
                        "AND MODULO  =  :financesModule \n" +
                        "AND NO_TRANS = :numberTransaction \n" +
                        "AND ESTADO <> 'APROBADO'"
        )
                .setParameter("financesModule", financesModule)
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .setParameter("numberTransaction",numberTransaction)
                .getResultList();
        for(Object[] data:datas)
        {
            entries.add(new ObsApprovedEntries((String)data[2],(String)data[6]));
        }

        return entries;

    }

    @Override
    public List<ObsApprovedEntries> getInfoTrasaction(String numberTransction) {
        List<ObsApprovedEntries> entries = new ArrayList<ObsApprovedEntries>();
        List<Object[]> datas = em.createNativeQuery("SELECT * FROM WISE.OBS_APROBACION_ASIENTOS\n" +
                        "WHERE  TRUNC(FECHA_SYS) = TRUNC(SYSDATE) \n" +
                        "AND NO_TRANS = :numberTransction"
        )
                .setParameter("numberTransction", numberTransction)
                .getResultList();
        for(Object[] data:datas)
        {
            entries.add(new ObsApprovedEntries((String)data[2],(String)data[6]));
        }

       return entries;

    }

    @Override
    public Voucher findVoucherByNoTrans(String transactionNumber){

        Voucher voucher = (Voucher)em.createNamedQuery("Voucher.findVoucherByNoTrans").setParameter("transactionNumber", transactionNumber).getSingleResult();

        return voucher;
    }

    public List<VoucherTransaction> getTransactionMajorAccounting(String start, String end, String cashAccount){

        List<VoucherTransaction> voucherTransactionList = new ArrayList<VoucherTransaction>();

        List<Object[]> resultList = em.createNativeQuery("select " +
                "e.fecha, d.cuenta, e.tipo_doc, e.no_doc, e.glosa, d.debe, d.haber " +
                "from sf_tmpdet d " +
                "left join sf_tmpenc e on d.id_tmpenc = e.id_tmpenc " +
                "where e.fecha BETWEEN '"+start+"' and '"+end+"' " +
                "and d.cuenta = '"+cashAccount+"' " +
                "and e.estado <> 'ANL' " +
                "order by e.fecha").getResultList();

        for(Object[] data:resultList){

            voucherTransactionList.add(new VoucherTransaction(DateUtils.format((Date)data[0], "dd/MM/yyyy"), (String)data[1], (String)data[2], (String)data[3], (String)data[4], (BigDecimal)data[5], (BigDecimal)data[6]));
        }


        return  voucherTransactionList;
    }

    public class VoucherTransaction{

        private String date;
        private String account;
        private String documentType;
        private String documentNumber;
        private String gloss;
        private BigDecimal debit;
        private BigDecimal credit;

        VoucherTransaction(String date, String account, String documentType, String documentNumber, String gloss, BigDecimal debit, BigDecimal credit){
            setDate(date);
            setAccount(account);
            setDocumentType(documentType);
            setDocumentNumber(documentNumber);
            setGloss(gloss);
            setDebit(debit);
            setCredit(credit);
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getDocumentNumber() {
            return documentNumber;
        }

        public void setDocumentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
        }

        public String getGloss() {
            return gloss;
        }

        public void setGloss(String gloss) {
            this.gloss = gloss;
        }

        public BigDecimal getDebit() {
            return debit;
        }

        public void setDebit(BigDecimal debit) {
            this.debit = debit;
        }

        public BigDecimal getCredit() {
            return credit;
        }

        public void setCredit(BigDecimal credit) {
            this.credit = credit;
        }
    }

    public class ObsApprovedEntries{
        private Date date;
        private String numberTransaction;
        private String state;
        private String module;
        private String observations;

        public ObsApprovedEntries(Date date, String numberTransaction, String state, String module, String observations) {
            this.date = date;
            this.numberTransaction = numberTransaction;
            this.state = state;
            this.module = module;
            this.observations = observations;
        }

        public ObsApprovedEntries(String numberTransaction, String observations) {
            this.numberTransaction = numberTransaction;
            this.observations = observations;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getNumberTransaction() {
            return numberTransaction;
        }

        public void setNumberTransaction(String numberTransaction) {
            this.numberTransaction = numberTransaction;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getObservations() {
            return observations;
        }

        public void setObservations(String observations) {
            this.observations = observations;
        }
    }
}
