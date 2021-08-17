package com.encens.khipus.service.accouting;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.admin.ProductSaleType;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.warehouse.InventoryPeriod;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.common.SequenceService;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.4
 */
@Stateless
@Name("voucherAccoutingService")
@AutoCreate
public class VoucherAccoutingServiceBean extends GenericServiceBean implements VoucherAccoutingService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(create = true)
    private FinancesPkGeneratorService financesPkGeneratorService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private SequenceService sequenceService;
    @In
    private PurchaseDocumentService purchaseDocumentService;
    @In
    private ProductionOrderService productionOrderService;
    @In
    private VoucherAccoutingService voucherAccoutingService;

    public void savePurchaseDocument(){

        PurchaseDocument pd = new PurchaseDocument();
        pd.setDate(new Date());
        pd.setNumber("164898028");
        pd.setName("ENCENS SRL");

        em.persist(pd);
        em.flush();

    }

    public void saveVoucher(Voucher voucher){

        Long id = financesPkGeneratorService.newId_sf_tmpenc();
        voucher.setId(id);

        if (voucher.getTransactionNumber() == null){
            voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
        }

        System.out.println("---> voucher.getDocumentNumber(): " + voucher.getDocumentNumber());
        if (voucher.getDocumentNumber() == null){
            voucher.setDocumentNumber(financesPkGeneratorService.getNextNoTransByDocumentType(voucher.getDocumentType()));
        }

        System.out.println("-------- VOUCHER ENCABEZADO -------");
        System.out.println("ID: " + voucher.getId());
        System.out.println("Fecha: " + voucher.getDate());
        System.out.println("NoTrans: " + voucher.getTransactionNumber());
        System.out.println("Tipo Doc: " + voucher.getDocumentType());
        System.out.println("Num Doc: " + voucher.getDocumentNumber());
        System.out.println("Descripcion: " + voucher.getDescription());
        System.out.println("Glosa: " + voucher.getGloss());

        em.persist(voucher);
        em.flush();

        System.out.println("-------- VOUCHER DETAILS -------");
        for (VoucherDetail voucherDetail : voucher.getDetails()) {
            voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
            voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
            voucherDetail.setVoucher(voucher);
            em.persist(voucherDetail);
            em.flush();
        }

    }

    public void updatePurchaseDocumentIfExist(Voucher voucher){

        for (VoucherDetail voucherDetail : voucher.getDetails()) {
            if (voucherDetail.getPurchaseDocument() != null){
                //System.out.println("-------------------------> debito fiscal: " + voucherDetail.getId());
                PurchaseDocument purchaseDocument = voucherDetail.getPurchaseDocument();
                purchaseDocument.setVoucherDetailFiscalCredit(voucherDetail);
                purchaseDocument.setVoucher(voucher);

                em.merge(purchaseDocument);
                em.flush();
            }
        }
    }

    public void removeVoucherDetail(VoucherDetail voucherDetail){
        em.remove(voucherDetail);
        em.flush();
    }

    /*@Override
    @TransactionAttribute(REQUIRES_NEW)
    public void updateVoucher(Voucher voucher) {

        *//** For VoucherDetail **//*
        List<VoucherDetail> voucherDetailsDB = getVoucherDetailList(voucher);
        for (VoucherDetail voucherDetail : voucherDetailsDB) {
            em.merge(voucherDetail);
            em.remove(voucherDetail);
            em.flush();
        }

        for (VoucherDetail voucherDetail : voucher.getDetails()) {
            if(voucherDetail.getTransactionNumber() == null){
                voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
                voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
                voucherDetail.setVoucher(voucher);

                em.persist(voucherDetail);
                em.merge(voucher);
                em.flush();

            }else{
                em.merge(voucherDetail);
                em.merge(voucher);
                em.flush();
            }
        }

        em.merge(voucher);
        em.flush();
    }*/

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void updateVoucher(Voucher voucher) {

        /** For VoucherDetail **/
        List<VoucherDetail> voucherDetailsDB = getVoucherDetailList(voucher);

        /*for (VoucherDetail voucherDetail : voucherDetailsDB) {
            em.merge(voucherDetail);
            em.remove(voucherDetail);
            em.flush();
        }*/

        for (VoucherDetail voucherDetail : voucher.getDetails()) {

            if(voucherDetail.getTransactionNumber() == null){
                voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
                voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
                voucherDetail.setVoucher(voucher);

                em.persist(voucherDetail);
                em.merge(voucher);
                em.flush();

            }else{
                em.merge(voucherDetail);
                em.merge(voucher);
                em.flush();
            }
        }

        em.merge(voucher);
        em.flush();
    }

    public void simpleUpdateVoucher(Voucher voucher){
        em.merge(voucher);
        em.flush();
    }

    public void createPurchaseDocumentVoucher(VoucherDetail voucherDetail){

        try {

            Voucher voucher = voucherDetail.getVoucher();

            PurchaseDocument purchaseDocument = voucherDetail.getPurchaseDocument();
            purchaseDocument.setVoucher(voucher);
            purchaseDocument.setNetAmount(purchaseDocument.getAmount());
            purchaseDocument.setType(CollectionDocumentType.INVOICE);
            purchaseDocumentService.createDocumentSimple(purchaseDocument);



            voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
            voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
            voucherDetail.setVoucher(voucher);

            em.persist(voucherDetail);
            em.merge(voucher);
            //em.flush();

            purchaseDocument.setVoucherDetailFiscalCredit(voucherDetail);
            em.merge(purchaseDocument);
            em.flush();

        }catch (Exception e){
            e.printStackTrace();
        }

        //em.flush();
    }

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void updateVoucherModify(Voucher voucher, List<VoucherDetail> voucherDetailList) {

        System.out.println("--> Modify");
        for (VoucherDetail voucherDetail : voucherDetailList){
            System.out.println("--> " + voucherDetail.getFullCashAccount() + " - " + voucherDetail.getDebit() + " - " + voucherDetail.getCredit());
        }

        List<VoucherDetail> voucherDetailsDB = getVoucherDetailList(voucher);
        System.out.println("--> DB" + voucher.getGloss());
        for (VoucherDetail voucherDetail : voucherDetailsDB) {
            System.out.println("--> " + voucherDetail.getFullCashAccount() + " - " + voucherDetail.getDebit() + " - " + voucherDetail.getCredit());
        }


        for (VoucherDetail voucherDetail : voucherDetailsDB){
            em.remove(voucherDetail);
            em.flush();
        }


        for (VoucherDetail voucherDetail : voucherDetailList){
            voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
            voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
            voucherDetail.setVoucher(voucher);

            em.persist(voucherDetail);
            //em.flush();
        }

        /*for (VoucherDetail voucherDetail : voucher.getDetails()) {
            if(voucherDetail.getTransactionNumber() == null){
                voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
                voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
                voucherDetail.setVoucher(voucher);

                em.persist(voucherDetail);
                em.merge(voucher);
                em.flush();

            }else{
                em.merge(voucherDetail);
                em.merge(voucher);
                em.flush();
            }
        }

        em.merge(voucher);
        em.flush();*/
    }

    @Override
    public void updateVoucher(Voucher voucher, PurchaseDocument purchaseDocument){

        purchaseDocument.setVoucher(voucher);

        em.merge(purchaseDocument);
        em.merge(voucher);

        em.flush();

    }

    @Override
    public void approveVoucher(Voucher voucher){
        em.merge(voucher);
        em.flush();
    }

    @Override
    public void annulVoucher(Voucher voucher){
        em.merge(voucher);
        em.flush();
    }

    @Override
    public List<VoucherDetail> getVoucherDetailList(String transactionNumber){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.transactionNumber = :transactionNumber ")
                    .setParameter("transactionNumber", transactionNumber)
                    .getResultList();
            /*voucherDetails = (List<VoucherDetail>) em.createNativeQuery("select * from sf_tmpdet where no_trans = :transactionNumber")
                    .setParameter("transactionNumber", transactionNumber).getResultList();*/
        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }

    @Override
    public List<VoucherDetail> getVoucherDetailList(Voucher voucher){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.voucher = :voucher ")
                    .setParameter("voucher", voucher)
                    .getResultList();
            /*voucherDetails = (List<VoucherDetail>) em.createNativeQuery("select * from sf_tmpdet where no_trans = :transactionNumber")
                    .setParameter("transactionNumber", transactionNumber).getResultList();*/
        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }

    @Override
    public List<PurchaseDocument> getPurchaseDcumentList(Voucher voucher){

        List<PurchaseDocument> purchaseDocumentList = new ArrayList<PurchaseDocument>();

        try {
            purchaseDocumentList = (List<PurchaseDocument>) em.createQuery("select purchaseDocument from PurchaseDocument purchaseDocument " +
                    " where purchaseDocument.voucher = :voucher ")
                    .setParameter("voucher", voucher)
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }
        return purchaseDocumentList;
    }

    @Override
    public Voucher getVoucher(String transactionNumber) {

        Voucher voucher = null;
        try {
            voucher = (Voucher) em.createQuery("select voucher from Voucher voucher " +
                    " where voucher.transactionNumber = :transactionNumber ")
                    .setParameter("transactionNumber", transactionNumber)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
        return voucher;
    }

    public Voucher getVoucher(Long id) {

        Voucher voucher = null;
        try {
            voucher = (Voucher) em.createQuery("select voucher from Voucher voucher " +
                    " where voucher.id = :id ")
                    .setParameter("id", id)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
        return voucher;
    }

    public Double getBalance(Date startDate, String cashAccountCode){

        List<VoucherDetail> voucherDetailList = new ArrayList<VoucherDetail>();

        try {
            voucherDetailList = em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " left join voucherDetail.voucher voucher " +
                    " where voucher.date < :startdate " +
                    " and voucherDetail.account = :cashAccountCode " +
                    " and voucher.state <> 'ANL' ")
                    .setParameter("startdate", startDate)
                    .setParameter("cashAccountCode", cashAccountCode)
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }

        Double balance = new Double("0.00");
        Double balanceD = new Double("0.00");
        Double balanceC = new Double("0.00");

        for (VoucherDetail voucherDetail : voucherDetailList){

            balanceD = balanceD + voucherDetail.getDebit().doubleValue();
            balanceC = balanceC + voucherDetail.getCredit().doubleValue();
        }

        balance = (balanceD - balanceC);

        return balance;
    }


    public Double getCustomerBalance(Date startDate, String cashAccountCode, Long clientId){

        List<VoucherDetail> voucherDetailList = new ArrayList<VoucherDetail>();

        try {
            voucherDetailList = em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " left join voucherDetail.voucher voucher " +
                    " where voucher.date < :startdate " +
                    " and voucherDetail.account = :cashAccountCode " +
                    " and voucherDetail.client.id = :clientId " +
                    " and voucher.state <> 'ANL'")
                    .setParameter("startdate", startDate)
                    .setParameter("cashAccountCode", cashAccountCode)
                    .setParameter("clientId", clientId)
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }

        Double balance = new Double("0.00");
        Double balanceD = new Double("0.00");
        Double balanceC = new Double("0.00");

        for (VoucherDetail voucherDetail : voucherDetailList){
            balanceD = balanceD + voucherDetail.getDebit().doubleValue();
            balanceC = balanceC + voucherDetail.getCredit().doubleValue();
        }
        balance = (balanceD - balanceC);

        System.out.println("----> SALDO CLIENTE: " + balance);

        return balance;
    }

    public Double getBalanceProvider(Date startDate, String cashAccountCode, String providerCode){

        List<VoucherDetail> voucherDetailList = new ArrayList<VoucherDetail>();

        try {
            /*voucherDetailList = em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " left join voucherDetail.voucher voucher " +
                    " where voucher.date < :startdate " +
                    " and voucherDetail.account = :cashAccountCode " +
                    " and voucher.providerCode = :providerCode " +
                    " and voucher.state <> 'ANL'")
                    .setParameter("startdate", startDate)
                    .setParameter("cashAccountCode", cashAccountCode)
                    .setParameter("providerCode", providerCode)
                    .getResultList();*/
            voucherDetailList = em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " left join voucherDetail.voucher voucher " +
                    " where voucher.date < :startdate " +
                    " and voucherDetail.account = :cashAccountCode " +
                    " and voucherDetail.providerCode = :providerCode " +
                    " and voucher.state <> 'ANL'")
                    .setParameter("startdate", startDate)
                    .setParameter("cashAccountCode", cashAccountCode)
                    .setParameter("providerCode", providerCode)
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }

        Double balance = new Double("0.00");
        Double balanceD = new Double("0.00");
        Double balanceC = new Double("0.00");

        for (VoucherDetail voucherDetail : voucherDetailList){
            balanceD = balanceD + voucherDetail.getDebit().doubleValue();
            balanceC = balanceC + voucherDetail.getCredit().doubleValue();
        }
        balance = (balanceD - balanceC);

        return balance;
    }

    public Double getTotalProfits(String startDate, String endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode AS accountCode, " +
                " rootCashAccount.description AS description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'I' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        Double totalProfits = new Double(0);
        try{

            datas = em.createQuery(ejbql).getResultList();

            Double totalDebit  = new Double(0);
            Double totalCredit = new Double(0);

            for(Object[] obj: datas){

                totalDebit  = totalDebit  + ((BigDecimal)obj[2]).doubleValue();
                totalCredit = totalCredit + ((BigDecimal)obj[3]).doubleValue();

            }

            totalProfits = totalCredit - totalDebit;

        }catch (NoResultException e){
            return null;
        }

        return totalProfits;
    }

    public Double getTotalLosses(String startDate, String endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode AS accountCode, " +
                " rootCashAccount.description AS description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'E' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        Double totalLosses = new Double(0);
        try{

            datas = em.createQuery(ejbql).getResultList();

            Double totalDebit  = new Double(0);
            Double totalCredit = new Double(0);

            for(Object[] obj: datas){

                totalDebit  = totalDebit  + ((BigDecimal)obj[2]).doubleValue();
                totalCredit = totalCredit + ((BigDecimal)obj[3]).doubleValue();

            }

            totalLosses = totalDebit - totalCredit;

        }catch (NoResultException e){
            return null;
        }

        return totalLosses;
    }

    public Double getTotalAssets(String startDate, String endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode AS accountCode, " +
                " rootCashAccount.description AS description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'A' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        Double totalAssets = new Double(0);
        try{

            datas = em.createQuery(ejbql).getResultList();

            Double totalDebit  = new Double(0);
            Double totalCredit = new Double(0);

            for(Object[] obj: datas){

                totalDebit  = totalDebit  + ((BigDecimal)obj[2]).doubleValue();
                totalCredit = totalCredit + ((BigDecimal)obj[3]).doubleValue();

            }

            totalAssets = totalDebit - totalCredit;

        }catch (NoResultException e){
            return null;
        }

        return totalAssets;
    }

    public Double getTotalLiabilities(String startDate, String endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode AS accountCode, " +
                " rootCashAccount.description AS description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'P' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        Double totalLiabilities = new Double(0);
        try{

            datas = em.createQuery(ejbql).getResultList();

            Double totalDebit  = new Double(0);
            Double totalCredit = new Double(0);

            for(Object[] obj: datas){

                totalDebit  = totalDebit  + ((BigDecimal)obj[2]).doubleValue();
                totalCredit = totalCredit + ((BigDecimal)obj[3]).doubleValue();

            }

            totalLiabilities = totalCredit - totalDebit;

        }catch (NoResultException e){
            return null;
        }

        return totalLiabilities;
    }

    public Double getTotalCapital(String startDate, String endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        String ejbql =  " SELECT " +
                " rootCashAccount.accountCode AS accountCode, " +
                " rootCashAccount.description AS description, " +
                " SUM(voucherDetail.debit) AS debit, " +
                " SUM(voucherDetail.credit) AS credit" +
                " FROM VoucherDetail voucherDetail " +
                " LEFT JOIN voucherDetail.voucher voucher " +
                " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                " WHERE cashAccount.accountType = 'C' " +
                " AND voucher.state <> 'ANL' " +
                " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                " GROUP BY rootCashAccount.accountCode, rootCashAccount.description ";

        Double totalCapital = new Double(0);
        try{

            datas = em.createQuery(ejbql).getResultList();

            Double totalDebit  = new Double(0);
            Double totalCredit = new Double(0);

            for(Object[] obj: datas){

                totalDebit  = totalDebit  + ((BigDecimal)obj[2]).doubleValue();
                totalCredit = totalCredit + ((BigDecimal)obj[3]).doubleValue();

            }

            totalCapital = totalCredit - totalDebit;

        }catch (NoResultException e){
            return null;
        }

        return totalCapital;
    }

    public BigDecimal totalCapital(String startDate, String endDate, String rootCashAccount){

        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal totalCapital = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            datas = em.createQuery(
                            " SELECT " +
                            " rootCashAccount.accountCode as accountCode, " +
                            " rootCashAccount.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                            " WHERE cashAccount.accountType = 'C' " +
                            " AND voucher.state <> 'ANL' " +
                            " AND rootCashAccount.accountCode = '"+rootCashAccount+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY rootCashAccount.accountCode, rootCashAccount.description "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[2]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[3]), 2);
            }
            totalCapital = BigDecimalUtil.subtract(totalCredit, totalDebit, 2);
        }catch (NoResultException e){}
        return totalCapital;
    }

    public BigDecimal perdidasExcedentesPeriodo(String startDate, String endDate){
        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal total        = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            datas = em.createQuery(
                    " SELECT " +
                            " voucher.documentType, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " WHERE voucher.documentType = 'SS' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY voucher.documentType "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[1]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[2]), 2);
            }
            total = BigDecimalUtil.subtract(totalCredit, totalDebit, 2);
        }catch (NoResultException e){}
        return total;

    }

    public HashMap<String, BigDecimal> stateBalanceReport(String start, String end){

        List<Object[]> datas = new ArrayList<Object[]>();
        HashMap<String, BigDecimal> totalResultStateBalance = new HashMap<String, BigDecimal>();

        String ejbql =  " SELECT " +
                        "        voucherDetail.account as account, " +          //0
                        "        cashAccount.description as description, " +    //1
                        "        SUM(voucherDetail.debit) as totalDebit, " +    //2
                        "        SUM(voucherDetail.credit) as totalCredit," +   //3
                        "        cashAccount.accountType as type " +            //4
                        "  FROM  VoucherDetail voucherDetail " +
                        "  LEFT  JOIN voucherDetail.voucher voucher " +
                        "  LEFT  JOIN voucherDetail.cashAccount cashAccount " +
                        "  WHERE voucher.date between '"+start+"' and '"+end+"' " +
                        "  AND   voucher.state <> 'ANL' " +
                        "  AND   voucher.documentType <> 'SS' " +
                        "  group by voucherDetail.account, cashAccount.description ";

        BigDecimal debit     = new BigDecimal(0);
        BigDecimal credit    = new BigDecimal(0);
        String accountType   = "";

        BigDecimal debitBalance     = new BigDecimal(0);
        BigDecimal creditBalance    = new BigDecimal(0);
        BigDecimal activeBalance    = new BigDecimal(0);
        BigDecimal passiveBalance   = new BigDecimal(0);
        BigDecimal egressBalance    = new BigDecimal(0);
        BigDecimal incomeBalance    = new BigDecimal(0);

        BigDecimal totalActiveBalance  = new BigDecimal(0);
        BigDecimal totalPassiveBalance = new BigDecimal(0);
        BigDecimal totalEgressBalance  = new BigDecimal(0);
        BigDecimal totalIncomeBalance  = new BigDecimal(0);


        try{
            datas = em.createQuery(ejbql).getResultList();

            for(Object[] data: datas){
                debit  = (BigDecimal)data[2];
                credit = (BigDecimal)data[3];
                accountType = ((CashAccountType)data[4]).toString();

                //System.out.println("----------> CashAccountType:" + accountType);

                /** For Debit Balance **/
                if ( debit.doubleValue() > credit.doubleValue() )
                    debitBalance = BigDecimalUtil.subtract(debit, credit, 2);
                else
                    debitBalance = BigDecimal.ZERO ;

                //$F{totalCredit}.doubleValue() > $F{totalDebit}.doubleValue() ? $F{totalCredit}.subtract($F{totalDebit}) : (new BigDecimal("0.00"))
                /** For Credit Balance **/
                if ( credit.doubleValue() > debit.doubleValue() )
                    creditBalance = BigDecimalUtil.subtract(credit, debit, 2);
                else
                    creditBalance = BigDecimal.ZERO;

                //($V{debitBalance}.doubleValue() > $V{creditBalance}.doubleValue()) && ( $F{type}.equals("A") || $F{type}.equals("P") || $F{type}.equals("C") ) ? $V{debitBalance} : (new BigDecimal("0.00"))
                /** For Active Balance **/
                if ( (debitBalance.doubleValue() > creditBalance.doubleValue()) && (accountType.equals("A") || accountType.equals("P") || accountType.equals("C")) )
                    activeBalance = debitBalance;
                else
                    activeBalance = BigDecimal.ZERO;

                //($V{creditBalance}.doubleValue() > $V{debitBalance}.doubleValue()) && ( $F{type}.equals("A") || $F{type}.equals("P") || $F{type}.equals("C") ) ? $V{creditBalance} : (new BigDecimal("0.00"))
                /** For Passive Balance **/
                if ( (creditBalance.doubleValue() > debitBalance.doubleValue()) && (accountType.equals("A") || accountType.equals("P") || accountType.equals("C")) )
                    passiveBalance = creditBalance;
                else
                    passiveBalance = BigDecimal.ZERO;

                //($V{debitBalance}.doubleValue() > $V{creditBalance}.doubleValue()) && ( $F{type}.equals("E") || $F{type}.equals("I") ) ? $V{debitBalance} : (new BigDecimal("0.00"))
                /** For Egress Balance **/
                if ( (debitBalance.doubleValue() > creditBalance.doubleValue()) && (accountType.equals("E") || accountType.equals("I")) )
                    egressBalance = debitBalance;
                else
                    egressBalance = BigDecimal.ZERO;

                //($V{creditBalance}.doubleValue() > $V{debitBalance}.doubleValue()) && ( $F{type}.equals("I") || $F{type}.equals("E") ) ? $V{creditBalance} : (new BigDecimal("0.00"))
                /** For Income Balance **/
                if ( (creditBalance.doubleValue() > debitBalance.doubleValue()) && (accountType.equals("E") || accountType.equals("I")) )
                    incomeBalance = creditBalance;
                else
                    incomeBalance = BigDecimal.ZERO;

                totalActiveBalance  = BigDecimalUtil.sum(totalActiveBalance, activeBalance, 2);
                totalPassiveBalance = BigDecimalUtil.sum(totalPassiveBalance, passiveBalance, 2);
                totalEgressBalance  = BigDecimalUtil.sum(totalEgressBalance, egressBalance, 2);
                totalIncomeBalance  = BigDecimalUtil.sum(totalIncomeBalance, incomeBalance, 2);
            }

            totalResultStateBalance.put("totalActiveBalance", totalActiveBalance);
            totalResultStateBalance.put("totalPassiveBalance", totalPassiveBalance);
            totalResultStateBalance.put("totalEgressBalance", totalEgressBalance);
            totalResultStateBalance.put("totalIncomeBalance", totalIncomeBalance);

        }catch (NoResultException e){ return null; }
        return  totalResultStateBalance;
    }

    /**  **/
    public BigDecimal calculateLossesNiv3(String startDate, String endDate, String accountLevel3){

        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal totalResult = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            /*datas = em.createQuery(
                    " SELECT " +
                            " rootCashAccount.accountCode as accountCode, " +
                            " rootCashAccount.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                            " WHERE cashAccount.accountType = 'C' " +
                            " AND voucher.state <> 'ANL' " +
                            " AND rootCashAccount.accountCode = '"+accountLevel3+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY rootCashAccount.accountCode, rootCashAccount.description "
            ).getResultList();*/

            datas = em.createQuery(
                            " SELECT " +
                            " cashAccountLeve3.accountCode as accountCode, " +
                            " cashAccountLeve3.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.cashAccountLeve3 cashAccountLeve3 " +
                            " WHERE cashAccount.accountType = 'E' " +
                            " AND voucher.state <> 'ANL' " +
                            " AND cashAccountLeve3.accountCode = '"+accountLevel3+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY cashAccountLeve3.accountCode, cashAccountLeve3.description "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[2]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[3]), 2);
            }
            totalResult = BigDecimalUtil.subtract(totalDebit, totalCredit, 2);
        }catch (NoResultException e){}
        return totalResult;
    }

    public BigDecimal calculateBalanceNiv3(String startDate, String endDate, String accountLevel3, String accountType){

        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal totalResult = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            datas = em.createQuery(
                    " SELECT " +
                            " cashAccountLeve3.accountCode as accountCode, " +
                            " cashAccountLeve3.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.cashAccountLeve3 cashAccountLeve3 " +
                            " WHERE cashAccount.accountType = '" + accountType + "'" +
                            " AND voucher.state <> 'ANL' " +
                            " AND cashAccountLeve3.accountCode = '"+accountLevel3+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY cashAccountLeve3.accountCode, cashAccountLeve3.description "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[2]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[3]), 2);
            }
            totalResult = BigDecimalUtil.subtract(totalDebit, totalCredit, 2);
        }catch (NoResultException e){}
        return totalResult;
    }

    public BigDecimal calculateLossesNiv2(String startDate, String endDate, String rootCashAccount){

        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal totalResult = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            datas = em.createQuery(
                    " SELECT " +
                            " rootCashAccount.accountCode as accountCode, " +
                            " rootCashAccount.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                            " WHERE cashAccount.accountType = 'E' " +
                            " AND voucher.state <> 'ANL' " +
                            " AND rootCashAccount.accountCode = '"+rootCashAccount+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY rootCashAccount.accountCode, rootCashAccount.description "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[2]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[3]), 2);
            }
            totalResult = BigDecimalUtil.subtract(totalDebit, totalCredit, 2);
        }catch (NoResultException e){}
        return totalResult;
    }

    public BigDecimal calculateBalanceNiv2(String startDate, String endDate, String rootCashAccount, String accountType){

        List<Object[]> datas = new ArrayList<Object[]>();

        BigDecimal totalResult = new BigDecimal(0);
        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        try {
            datas = em.createQuery(
                    " SELECT " +
                            " rootCashAccount.accountCode as accountCode, " +
                            " rootCashAccount.description as description, " +
                            " SUM(voucherDetail.debit) AS debit, " +
                            " SUM(voucherDetail.credit) AS credit" +
                            " FROM VoucherDetail voucherDetail " +
                            " LEFT JOIN voucherDetail.voucher voucher " +
                            " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                            " LEFT JOIN voucherDetail.cashAccount.rootCashAccount rootCashAccount " +
                            " WHERE cashAccount.accountType = '" + accountType + "'" +
                            " AND voucher.state <> 'ANL' " +
                            " AND rootCashAccount.accountCode = '"+rootCashAccount+"' " +
                            " AND voucher.date between '"+startDate+"' and '"+endDate+"' " +
                            " GROUP BY rootCashAccount.accountCode, rootCashAccount.description "
            ).getResultList();

            for(Object[] obj: datas){
                totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)obj[2]), 2);
                totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)obj[3]), 2);
            }
            totalResult = BigDecimalUtil.subtract(totalDebit, totalCredit, 2);
        }catch (NoResultException e){}
        return totalResult;
    }


    /** MODIFYID **/
    public void createCostOfSale_MilkProducts(Date startDate, Date endDate, Warehouse warehouse) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<Object[]> sales = em.createNativeQuery("select v.cod_art, sum(v.cantidad) " +
                "from ventas v " +
                "where v.fecha between :startDate and :endDate " +
                "and v.cod_alm =:cod_alm " +
                "and v.idtipopedido in (1, 5) " +
                "group by v.cod_art ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("cod_alm", warehouse.getWarehouseCode())
                .getResultList();

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas (" + warehouse.getName() + ") "
                + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " "
                + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        System.out.println("----------SALES---------");
        for (Object[] sale : sales){
            System.out.println("---> sales: |" + (String)sale[0] + "|" + (BigDecimal)sale[1]);
        }
        System.out.println("----------END SALES---------");

        //createVoucherDetailForCostOfSales(sales, voucher, companyConfiguration.getCtaCostPT().getAccountCode(), startDate, endDate);
        createVoucherDetailForCostOfSales(sales, voucher, warehouse, startDate, endDate);


    }

    /** Reposiciones + Promociones **/
    public void createCostOfSale_MilkProductsReplacement(Date startDate, Date endDate, Warehouse warehouse) throws CompanyConfigurationNotFoundException {

        List<Object[]> sales = em.createNativeQuery("" +
                "SELECT z.cod_art, SUM(z.cantidad) " +
                "FROM ( " +
                "" +
                "SELECT v.cod_art, SUM(v.REPOSICION) + SUM(v.promocion) + SUM(v.cantidad) AS cantidad " + /*Tipo de pedido reposicion*/
                "FROM ventas v  " +
                "WHERE v.fecha BETWEEN :startDate AND :endDate " +
                "AND v.`cod_alm` =:cod_alm " +
                "AND v.idtipopedido IN (4) " + /*Pedidos tipo Reposicion*/
                "GROUP BY v.cod_art " +
                "UNION " +
                "" +
                "SELECT v.cod_art, SUM(v.REPOSICION) + SUM(v.promocion) AS cantidad " + /*Reposicion, promocion pedidos normales (contado, credito) */
                "FROM ventas v " +
                "WHERE v.fecha BETWEEN :startDate AND :endDate " +
                "AND v.`cod_alm` =:cod_alm " +
                "AND v.idtipopedido IN (1) " +
                "GROUP BY v.cod_art " +
                ") z " +
                "GROUP BY z.cod_art ")
                .setParameter("startDate", startDate).setParameter("endDate", endDate)
                .setParameter("cod_alm", warehouse.getWarehouseCode())
                .getResultList();


        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas por REPOSICIONES y PROMOCIONES (" + warehouse.getName() + ") "
                + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        //createVoucherDetailForCostOfSales(sales, voucher, "4470510300", startDate, endDate);
        createVoucherDetailForCostOfSales(sales, voucher, warehouse, startDate, endDate);
    }

    /** Degustacion o Refrigerio **/
    public void createCostOfSale_MilkProductsTastingOrRefreshment(Date startDate, Date endDate, Long orderTypeId, String costAccount, String cod_alm) throws CompanyConfigurationNotFoundException {

        List<Object[]> sales = em.createNativeQuery("" +
                "SELECT v.cod_art, SUM(v.cantidad) AS cantidad " +
                "FROM ventas v " +
                "WHERE v.fecha BETWEEN :startDate AND :endDate " +
                "AND v.`cod_alm` =:cod_alm " +
                "AND v.idtipopedido =:orderTypeId " +
                "GROUP BY v.cod_art " )
                .setParameter("startDate", startDate).setParameter("endDate", endDate)
                .setParameter("cod_alm", cod_alm)
                .setParameter("orderTypeId", orderTypeId)
                .getResultList();

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);

        String tastingOrRefreshment = "DEGUSTACIONES";
        if (orderTypeId == 3)
            tastingOrRefreshment = "REFRIGERIOS";

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas por " + tastingOrRefreshment +" en " + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        createVoucherDetailForCostOfSales(sales, voucher, costAccount, startDate, endDate);
    }

    public void createCostOfSale_VeterinaryProducts(Date startDate, Date endDate, String cod_alm) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<Object[]> sales = em.createNativeQuery("" +
                "select v.cod_art, sum(v.cantidad) " +
                "from ventas v where v.fecha between :startDate and :endDate " +
                "and v.cod_alm =:cod_alm " +
                "group by v.cod_art ")
                .setParameter("startDate", startDate).setParameter("endDate", endDate)
                .setParameter("cod_alm", cod_alm)
                .getResultList();

        HashMap<String, BigDecimal> unitCostVeterinaryProducts = getUnitCost_veterinaryProducts(startDate, endDate);
        BigDecimal totalCost = BigDecimal.ZERO;

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas " + MessageUtils.getMessage(ProductSaleType.VETERINARY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        VoucherDetail voucherDebit = new VoucherDetail();
        voucher.addVoucherDetail(voucherDebit);
        voucherDebit.setAccount(companyConfiguration.getCtaCostPV().getAccountCode());
        voucherDebit.setDebit(totalCost);
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);

        totalCost = BigDecimal.ZERO;
        for (Object[] sale : sales){
            String codArt = (String)sale[0];
            BigDecimal quantity = (BigDecimal) sale[1];

            /** Uncomment for test **/
            System.out.println("=============> Create CV for: " + codArt + " - " + quantity);

            if (quantity.doubleValue() > 0){
                BigDecimal unitCost = unitCostVeterinaryProducts.get(codArt);
                if (unitCost.doubleValue() > 0){
                    BigDecimal cost = BigDecimalUtil.multiply(quantity, unitCost, 2);
                    totalCost = BigDecimalUtil.sum(totalCost, cost, 2);

                    VoucherDetail voucherCredit = new VoucherDetail();
                    voucherCredit.setAccount(companyConfiguration.getCtaAlmPV().getAccountCode());
                    voucherCredit.setDebit(BigDecimal.ZERO);
                    voucherCredit.setCredit(BigDecimalUtil.roundBigDecimal(cost,2));

                    voucherCredit.setProductItemCode(codArt);
                    voucherCredit.setQuantityArt(quantity);

                    voucherCredit.setCurrency(FinancesCurrencyType.P);
                    voucherCredit.setExchangeAmount(BigDecimal.ONE);
                    voucherCredit.setDebitMe(BigDecimal.ZERO);
                    voucherCredit.setCreditMe(BigDecimal.ZERO);
                    voucher.addVoucherDetail(voucherCredit);
                }
            }
        }
        voucherDebit.setDebit(totalCost);
        saveVoucher(voucher);
    }


    /** MODIFYID **/
    public void createCostOfSale_MilkProducts(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<Object[]> sales = em.createNativeQuery("select v.cod_art, sum(v.cantidad) " +
                "from ventas v " +
                "where v.fecha between :startDate and :endDate " +
                "and v.idusuario not in (408, 5) and v.idtipopedido in (1,5) " +
                "and v.cod_art not in (192,193,194,795,195,196,188,493,693,834,835) " + /** MODIFYID **/
                "group by v.cod_art ").setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas " + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        System.out.println("----------SALES---------");
        for (Object[] sale : sales){
            System.out.println("---> sales: |" + (String)sale[0] + "|" + (BigDecimal)sale[1]);
        }
        System.out.println("----------END SALES---------");

        createVoucherDetailForCostOfSales(sales, voucher, companyConfiguration.getCtaCostPT().getAccountCode(), startDate, endDate);


    }

    /** Reposiciones + Promociones **/
    public void createCostOfSale_MilkProductsReplacement(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        List<Object[]> sales = em.createNativeQuery("" +
                "select z.cod_art, sum(z.cantidad) " +
                "from ( " +
                "       select a.cod_art, SUM(a.REPOSICION) + SUM(a.PROMOCION) AS cantidad " +
                "       from articulos_pedido a " +
                "       left join pedidos p on a.idpedidos = p.idpedidos " +
                "       where p.fecha_entrega between :startDate and :endDate " +
                "       and p.idtipopedido = 1 and p.estado <> 'ANULADO' " +
                "       group by a.cod_art " +
                "       union " +
                "       select a.cod_art, sum(a.cantidad) as cantidad " +
                "       from articulos_pedido a " +
                "       left join pedidos p on a.idpedidos = p.idpedidos " +
                "       where p.fecha_entrega between :startDate and :endDate " +
                "       and p.idtipopedido = 4 and p.estado <> 'ANULADO' " +
                "       group by a.cod_art " +
                "       union " +
                "       select a.cod_art, sum(a.reposicion) + sum(a.promocion) as cantidad " +
                "       from articulos_pedido a " +
                "       left join ventadirecta v on a.idventadirecta = v.idventadirecta " +
                "       where v.fecha_pedido between :startDate and :endDate " +
                "       and v.estado <> 'ANULADO'" +
                "       group by a.cod_art" +
                ") z " +
                "group by z.cod_art ").setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();


        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas por REPOSICIONES y PROMOCIONES en " + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        createVoucherDetailForCostOfSales(sales, voucher, "4470510300", startDate, endDate);
    }

    /** Degustacion o Refrigerio **/
    public void createCostOfSale_MilkProductsTastingOrRefreshment(Date startDate, Date endDate, Long orderTypeId, String costAccount) throws CompanyConfigurationNotFoundException {

        List<Object[]> sales = em.createNativeQuery("" +
                "select a.cod_art, sum(a.cantidad) as degustacion " +
                "from articulos_pedido a " +
                "left join pedidos p on a.idpedidos = p.idpedidos " +
                "where p.fecha_entrega between :startDate and :endDate " +
                "and p.idtipopedido = :orderTypeId and p.estado <> 'ANULADO' " +
                "group by a.cod_art " ).setParameter("startDate", startDate).setParameter("endDate", endDate).setParameter("orderTypeId", orderTypeId).getResultList();


        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);

        String tastingOrRefreshment = "DEGUSTACIONES";
        if (orderTypeId == 3)
            tastingOrRefreshment = "REFRIGERIOS";

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas por " + tastingOrRefreshment +" en " + MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        createVoucherDetailForCostOfSales(sales, voucher, costAccount, startDate, endDate);
    }

    public void createVoucherDetailForCostOfSales(List<Object[]> sales, Voucher voucher, String costAccount, Date startDate, Date endDate)throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = getUnitCost_milkProducts(startDate, endDate);
        BigDecimal totalCost = BigDecimal.ZERO;

        VoucherDetail voucherDebit = new VoucherDetail();
        voucher.addVoucherDetail(voucherDebit);
        voucherDebit.setAccount(costAccount);
        voucherDebit.setDebit(totalCost);
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);

        for (Object[] sale : sales){
            String codArt = (String)sale[0];
            BigDecimal quantity = (BigDecimal) sale[1];

            if (quantity.doubleValue() > 0){
                BigDecimal unitCost = unitCostMilkProducts.get(codArt);
                System.out.println("==========================> COD_ART: " + codArt);
                if (unitCost.doubleValue() > 0){
                    BigDecimal cost = BigDecimalUtil.multiply(quantity, unitCost, 6);
                    System.out.println("=============Create-CV=============> COD_ART: " + codArt + " - unitCost: " + unitCost + " - Cost: " + cost);
                    VoucherDetail voucherCredit = new VoucherDetail();
                    voucherCredit.setAccount(companyConfiguration.getCtaAlmPT().getAccountCode());
                    voucherCredit.setDebit(BigDecimal.ZERO);
                    voucherCredit.setCredit(BigDecimalUtil.roundBigDecimal(cost,2));
                    voucherCredit.setProductItemCode(codArt);
                    voucherCredit.setQuantityArt(quantity);
                    voucherCredit.setCurrency(FinancesCurrencyType.P);
                    voucherCredit.setExchangeAmount(BigDecimal.ONE);
                    voucherCredit.setDebitMe(BigDecimal.ZERO);
                    voucherCredit.setCreditMe(BigDecimal.ZERO);
                    voucher.addVoucherDetail(voucherCredit);

                    totalCost = BigDecimalUtil.sum(totalCost, voucherCredit.getCredit(), 2);
                }
            }
        }
        voucherDebit.setDebit(totalCost);
        saveVoucher(voucher);
    }

    public void createVoucherDetailForCostOfSales(List<Object[]> sales, Voucher voucher, Warehouse warehouse, Date startDate, Date endDate)throws CompanyConfigurationNotFoundException {

        //CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        HashMap<String, BigDecimal> unitCostMilkProducts = getUnitCost_milkProducts(startDate, endDate);
        BigDecimal totalCost = BigDecimal.ZERO;

        VoucherDetail voucherDebit = new VoucherDetail();
        voucher.addVoucherDetail(voucherDebit);
        voucherDebit.setAccount(warehouse.getCashAccountCost());
        voucherDebit.setDebit(totalCost);
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);

        for (Object[] sale : sales){
            String codArt = (String)sale[0];
            BigDecimal quantity = (BigDecimal) sale[1];

            if (quantity.doubleValue() > 0){
                BigDecimal unitCost = unitCostMilkProducts.get(codArt);
                System.out.println("==========================> COD_ART: " + codArt);
                if (unitCost.doubleValue() > 0){
                    BigDecimal cost = BigDecimalUtil.multiply(quantity, unitCost, 6);
                    System.out.println("=============Create-CV By Warehouse=============> COD_ART: " + codArt + " - unitCost: " + unitCost + " - Cost: " + cost);
                    VoucherDetail voucherCredit = new VoucherDetail();
                    voucherCredit.setAccount(warehouse.getCashAccount());
                    voucherCredit.setDebit(BigDecimal.ZERO);
                    voucherCredit.setCredit(BigDecimalUtil.roundBigDecimal(cost,2));
                    voucherCredit.setProductItemCode(codArt);
                    voucherCredit.setQuantityArt(quantity);
                    voucherCredit.setCurrency(FinancesCurrencyType.P);
                    voucherCredit.setExchangeAmount(BigDecimal.ONE);
                    voucherCredit.setDebitMe(BigDecimal.ZERO);
                    voucherCredit.setCreditMe(BigDecimal.ZERO);
                    voucher.addVoucherDetail(voucherCredit);

                    totalCost = BigDecimalUtil.sum(totalCost, voucherCredit.getCredit(), 2);
                }
            }
        }
        voucherDebit.setDebit(totalCost);
        saveVoucher(voucher);
    }

    public void createCostOfSale_VeterinaryProducts(Date startDate, Date endDate) throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<Object[]> sales = em.createNativeQuery("select v.cod_art, sum(v.cantidad) " +
                "from ventas v where v.fecha between :startDate and :endDate and v.idusuario in (408, 5) " +
                "group by v.cod_art ").setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

        HashMap<String, BigDecimal> unitCostVeterinaryProducts = getUnitCost_veterinaryProducts(startDate, endDate);
        BigDecimal totalCost = BigDecimal.ZERO;

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas " + MessageUtils.getMessage(ProductSaleType.VETERINARY_PRODUCT.getResourceKey()) + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        VoucherDetail voucherDebit = new VoucherDetail();
        voucher.addVoucherDetail(voucherDebit);
        voucherDebit.setAccount(companyConfiguration.getCtaCostPV().getAccountCode());
        voucherDebit.setDebit(totalCost);
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);

        totalCost = BigDecimal.ZERO;
        for (Object[] sale : sales){
            String codArt = (String)sale[0];
            BigDecimal quantity = (BigDecimal) sale[1];

            /** Uncomment for test **/
            System.out.println("=============> Create CV for: " + codArt + " - " + quantity);

            if (quantity.doubleValue() > 0){
                BigDecimal unitCost = unitCostVeterinaryProducts.get(codArt);
                if (unitCost.doubleValue() > 0){
                    BigDecimal cost = BigDecimalUtil.multiply(quantity, unitCost, 2);
                    totalCost = BigDecimalUtil.sum(totalCost, cost, 2);

                    VoucherDetail voucherCredit = new VoucherDetail();
                    voucherCredit.setAccount(companyConfiguration.getCtaAlmPV().getAccountCode());
                    voucherCredit.setDebit(BigDecimal.ZERO);
                    voucherCredit.setCredit(BigDecimalUtil.roundBigDecimal(cost,2));

                    voucherCredit.setProductItemCode(codArt);
                    voucherCredit.setQuantityArt(quantity);

                    voucherCredit.setCurrency(FinancesCurrencyType.P);
                    voucherCredit.setExchangeAmount(BigDecimal.ONE);
                    voucherCredit.setDebitMe(BigDecimal.ZERO);
                    voucherCredit.setCreditMe(BigDecimal.ZERO);
                    voucher.addVoucherDetail(voucherCredit);
                }
            }
        }
        voucherDebit.setDebit(totalCost);
        saveVoucher(voucher);
    }

    public HashMap<String, BigDecimal> getUnitCost_veterinaryProducts(Date startDate, Date endDate){

        HashMap<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        String gestion = DateUtils.getCurrentYear(startDate).toString();
        String month = DateUtils.getCurrentMonth(startDate).toString();

        List<Object[]> purchaseList = em.createNativeQuery("" +
                "select z.cod_art, sum(z.monto) / sum(z.cantidad) from ( " +
                " " + /** Inventario inicio de periodo **/
                "   SELECT p.cod_art, (p.saldofis * p.costouni) as monto, p.saldofis as cantidad " +
                "   FROM inv_periodo p " +
                "   WHERE p.cod_alm = 5 " +
                "   AND p.mes = :month " +
                "   AND p.gestion = :gestion " +
                "   AND p.saldofis > 0 " +
                "   UNION " +
                " " + /** Compras **/
                "   select d.cod_art, d.monto, d.cantidad " +
                "   from inv_movdet d " +
                "   left join inv_vales v on d.no_trans = v.no_trans " +
                "   where v.fecha between :startDate and :endDate " +
                "   and v.cod_alm = 5 and d.tipo_mov = 'E' and v.id_com_encoc is not null " +
                ") z " +
                "group by z.cod_art " +
                ";")
                .setParameter("month", month).setParameter("startDate", startDate).setParameter("endDate", endDate).setParameter("gestion", gestion).getResultList();

        for (Object[] purchase : purchaseList){
            String codArt = (String) purchase[0];
            BigDecimal unitCost = (BigDecimal) purchase[1];
            result.put(codArt, unitCost);
            /** Uncomment for test **/
            System.out.println("----> MAP: " + codArt + "\t " + unitCost);
        }

        return result;
    }

    public HashMap<String, BigDecimal> getUnitCost_milkProducts(Date startDate, Date endDate){

        /** MODIFYID **/
        HashMap<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        String gestion = DateUtils.getCurrentYear(startDate).toString();
        String month = DateUtils.getCurrentMonth(startDate).toString();
        System.out.println("=============> MES : " + month);
        List<Object[]> productList = em.createNativeQuery("" +
                "SELECT z.cod_art, (SUM(z.debe)-SUM(z.haber)) / (SUM(z.cant_d)-SUM(z.cant_h)) AS costo_uni " +
                "FROM ( " +
                "  " + /** Inventario inicio de periodo **/
                " SELECT p.cod_art, (p.saldofis * p.costouni) AS debe, 0 AS haber, p.saldofis AS cant_d, 0 AS cant_h " +
                " FROM inv_periodo p " +
                " WHERE p.cod_alm = 2 " +
                " AND p.mes = :month " +
                " AND p.gestion = :gestion " +
                " AND p.saldofis > 0 " +
                " UNION " +
                " " + /** Compras **/
                " SELECT d.cod_art, SUM(d.monto) AS debe, 0 AS haber, SUM(d.cantidad) AS cant_d, 0 AS cant_h " +
                " FROM inv_movdet d " +
                " LEFT JOIN inv_vales v ON d.no_trans = v.no_trans " +
                " WHERE v.fecha BETWEEN  :startDate AND  :endDate " +
                " AND v.cod_alm = 2 AND d.tipo_mov = 'E' AND v.id_com_encoc IS NOT NULL " +
                " GROUP BY d.cod_art " +
                " UNION " +
                " " + /** Entradas de produccion **/
                " SELECT d.cod_art, SUM(d.monto) AS debe, 0 AS haber, SUM(d.cantidad) cant_d, 0 AS cant_h " +
                " FROM inv_vales i " +
                " JOIN inv_movdet d ON i.no_trans = d.no_trans " +
                " WHERE i.fecha BETWEEN  :startDate AND  :endDate " +
                " AND i.cod_alm = 2 " +
                " AND (i.idordenproduccion IS NOT NULL OR i.idproductobase IS NOT NULL) " +
                " GROUP BY d.cod_art " +
                " UNION " +
                " " + /** Entradas de produccion 2 **/
                " SELECT d.cod_art, SUM(d.debe) AS debe, 0 AS haber, SUM(d.cant_art) cant_d, 0 AS cant_h " +
                " FROM sf_tmpdet d " +
                " LEFT JOIN sf_tmpenc e ON d.id_tmpenc = e.id_tmpenc " +
                " WHERE e.fecha BETWEEN :startDate AND :endDate " +
                " AND d.cuenta = '1510110201' " +
                " AND d.debe > 0 " +
                " GROUP BY d.cod_art " +
                " UNION " +
                " " + /** Transferencias, Bajas, Devoluciones **/
                " SELECT d. cod_art , SUM(t. debe ) AS debe, SUM(t. haber ) AS haber, SUM(IF(t.debe>0, d.cantidad, 0)) AS cant_d, SUM(IF(t.haber>0, d.cantidad, 0)) AS cant_h " +
                " FROM inv_movdet d " +
                " LEFT JOIN inv_vales v ON d. no_trans  = v. no_trans  " +
                " LEFT JOIN sf_tmpdet t ON v. idtmpenc  = t. id_tmpenc  " +
                " WHERE v. fecha  BETWEEN  :startDate AND  :endDate " +
                " AND v. oper  IS NOT NULL " +
                " AND d. cod_art  = t. cod_art  " +
                " GROUP BY d. cod_art  " +
                ") z " +
                "GROUP BY z.cod_art")
                .setParameter("month", month).setParameter("gestion", gestion).setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

        System.out.println("----------CALCULANDO--UNITCOST--PT------------");
        for (Object[] product : productList){
            String codArt = (String) product[0];
            BigDecimal unitCost = (BigDecimal) product[1];
            result.put(codArt, unitCost);
            /** Uncomment for test **/
            System.out.println("----> MAP PRODUCT: |" + codArt + "|" + unitCost);
        }
        System.out.println("---------------END------------");

        //result.put("148", result.get("151")); // EDAM
        //result.put("150", result.get("151")); // FRESCO
        //result.put("643", result.get("118")); // FLUIDA // revisar, calcular cuando producen
        //result.put("761", result.get("139")); // BEBIDA LACTEA FERMENTADA 100ML (FRUTILLA)/139 - YOGURT SACHET FRUTILLA 120 CC /
        //result.put("760", result.get("154")); // BEBIDA LACTEA FERMENTADA 100ML (DURAZNO)/154 - YOGURT SACHET DURAZNO 120 CC
        //result.put("762", result.get("157")); // BEBIDA LACTEA FERMENTADA 100ML (COCO)/154 - YOGURT SACHET COCO 120 CC
        //result.put("763", result.get("588")); // 763-BEBIDA LACTEA 120ML (MANZANA) / 588 - ILFRUT MANZANA 120 ML
        //result.put("764", result.get("589")); // 764-BEBIDA LACTEA 120ML (DURAZNO) / 589 - ILFRUT DURAZNO 120 ML

        //if (result.get("643") == null)              // 643-LECHE FLUIDA UHT 946 ML
        //    result.put("643", result.get("118"));   // 118-LECHE UHT 950ML

        /*if (result.get("704") == null) //704-YOG BEBIBLE FAMILIAR DURAZNO 1L
            result.put("704", result.get("703")); //703-YOG BEBIBLE FAMILIAR FRUTILLA 1L

        if (result.get("705") == null) //705-YOG BEBIBLE FAMILIAR coco 1L
            result.put("705", result.get("703")); //703-YOG BEBIBLE FAMILIAR FRUTILLA 1L*/

        if (result.get("668") == null) //668-YOGURT BEBIBLE 1L DURAZNO
            result.put("668", result.get("704")); //704-YOG BEBIBLE FAMILIAR DURAZNO 1L
        if (result.get("669") == null) //669-YOGURT BEBIBLE 1L COCO
            result.put("669", result.get("705")); //705-YOG BEBIBLE FAMILIAR COCO 1L
        if (result.get("642") == null) //642-YOGURT BEBIBLE 1L FRUTILLA
            result.put("642", result.get("703")); //703-YOG BEBIBLE FAMILIAR FRUTILLA 1L

        result.put("188", BigDecimal.ZERO);
        result.put("193", BigDecimal.ZERO);
        result.put("195", BigDecimal.ZERO);
        result.put("196", BigDecimal.ZERO);
        result.put("197", BigDecimal.ZERO);
        result.put("490", BigDecimal.ZERO);
        result.put("493", BigDecimal.ZERO);
        result.put("521", BigDecimal.ZERO);
        result.put("693", BigDecimal.ZERO);

        /** Adicionando lista de productos equivalentes (Productos agencia) **/
        List<ProductItem> productItemEqList = getProductItemEqList();
        for (ProductItem productItem : productItemEqList){
            result.put(productItem.getProductItemCodeEq(), result.get(productItem.getProductItemCode()));
        }

        return result;
    }

    public BigDecimal getUnitCost_lastPeriod(String productItemCode){
        BigDecimal result = null;

        List<InventoryPeriod> inventoryPeriodList = new ArrayList<InventoryPeriod>();

        inventoryPeriodList = (List<InventoryPeriod>) em.createQuery(
                "Select i from InventoryPeriod i " +
                   "where i.productItemCode =:productItemCode " +
                   "and i.quantity > 0")
                .setParameter("productItemCode", productItemCode)
                .getResultList();

        if (inventoryPeriodList.size() > 0){
            result = inventoryPeriodList.get(inventoryPeriodList.size()-1).getUnitCost();
        }

        System.out.println("-------------------> unitCost:" + result);

        return  result;
    }

    public List<ProductItem> getProductItemEqList(){
        HashMap<String, String> result = new HashMap<String, String>();
        List<ProductItem> productItemList = new ArrayList<ProductItem>();
        productItemList = (List<ProductItem>) em.createQuery("select p from ProductItem p " +
                " where p.productItemCodeEq is not null")
                .getResultList();

        /*for (ProductItem productItem : productItemList){
            result.put(productItem.getProductItemCode(), productItem.getProductItemCodeEq());
        }
        return result;*/
        return productItemList;
    }


    public BigDecimal getUnitCost_milkProduct(String codArt, Date startDate, Date endDate){

        /** MODIFYID **/
        if (codArt.equals("148") || codArt.equals("150"))
            codArt = "151";
        if (codArt.equals("643"))
            codArt = "118";

        if (codArt.equals("195") || codArt.equals("196") || codArt.equals("197") || codArt.equals("490") || codArt.equals("521"))
            return BigDecimal.ZERO;

        BigDecimal result = (BigDecimal) em.createNativeQuery("select (sum(t.costototalproduccion)/sum(t.cant_total))" +
                "from producciontotal t " +
                "where t.fecha between :startDate and :endDate " +
                "and t.cod_art = :codArt " +
                "group by t.cod_art, t.nombre")
        .setParameter("startDate", startDate)
        .setParameter("endDate", endDate)
        .setParameter("codArt", codArt)
        .getSingleResult();

        return result;
    }

    public void createCostOfCashSales(Date startDate, Date endDate, ProductSaleType productSaleType) throws CompanyConfigurationNotFoundException {

        List<VentaDirecta> sales = null;
        BigDecimal totalCost = new BigDecimal(0.0);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        /** DAIRY_PRODUCT **/
        CashAccount ctaCost = companyConfiguration.getCtaCostPT();
        CashAccount ctaAlm  = companyConfiguration.getCtaAlmPT();
        String produtTypeMessage = MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey());

        try {

            if (ProductSaleType.DAIRY_PRODUCT.equals(productSaleType)){
                sales = em.createNamedQuery("VentaDirecta.findByDatesForCostsLac")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            }

            if (ProductSaleType.VETERINARY_PRODUCT.equals(productSaleType)){
                ctaCost = companyConfiguration.getCtaCostPV();
                ctaAlm  = companyConfiguration.getCtaAlmPV();
                sales = em.createNamedQuery("VentaDirecta.findByDatesForCostsVet")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
                produtTypeMessage = MessageUtils.getMessage(ProductSaleType.VETERINARY_PRODUCT.getResourceKey());
            }

        } catch (NoResultException e) {}

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas al contado " + produtTypeMessage + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);

        if (sales.size() > 0) {
            for (VentaDirecta sale : sales) {
                for (ArticleOrder articleOrder : sale.getArticulosPedidos())
                    totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.multiply(articleOrder.getCu(), BigDecimalUtil.toBigDecimal(articleOrder.getQuantity())));
                sale.setCvFlag(true);
            }

            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                    ctaCost,
                    totalCost,
                    FinancesCurrencyType.P, null));

            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                    ctaAlm,
                    totalCost,
                    FinancesCurrencyType.P, null));

            voucher.setDate(endDate);
            voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
            saveVoucher(voucher);
        }
    }

    public void createCostOfSalesCredit(Date startDate, Date endDate, ProductSaleType productSaleType) throws CompanyConfigurationNotFoundException{

        List<CustomerOrder> sales = null;
        BigDecimal totalCost = new BigDecimal(0.0);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        /** DAIRY_PRODUCT **/
        CashAccount ctaCost = companyConfiguration.getCtaCostPT();
        CashAccount ctaAlm  = companyConfiguration.getCtaAlmPT();
        String produtTypeMessage = MessageUtils.getMessage(ProductSaleType.DAIRY_PRODUCT.getResourceKey());

        List<Object[]> salesList = new ArrayList<Object[]>();

        try {
            if (ProductSaleType.DAIRY_PRODUCT.equals(productSaleType)){
                salesList = em.createNativeQuery("select a.cod_art, sum(a.cantidad), (sum(a.cantidad) * a.cu) " +
                        "from articulos_pedido a " +
                        "join pedidos p on a.idpedidos = p.idpedidos " +
                        "where p.fecha_entrega between :startDate and :endDate " +
                        "and p.idtipopedido in (1, 5) " + //Pedido normal y desc. lacteo
                        "and p.estado <> 'ANULADO' " +
                        "and p.idusuario <> 5 " +
                        "group by a.cod_art ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

            }

            if (ProductSaleType.VETERINARY_PRODUCT.equals(productSaleType)){
                ctaCost = companyConfiguration.getCtaCostPV();
                ctaAlm  = companyConfiguration.getCtaAlmPV();

                salesList = em.createNativeQuery("select a.cod_art, sum(a.cantidad), (sum(a.cantidad) * a.cu) " +
                        "from articulos_pedido a " +
                        "join pedidos p on a.idpedidos = p.idpedidos " +
                        "where p.fecha_entrega between :startDate and :endDate " +
                        "and p.idtipopedido in (1, 6) " + //Pedido normal y desc. veterinario
                        "and p.estado <> 'ANULADO' " +
                        "and p.idusuario = 5 " +
                        "group by a.cod_art ")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();

                /* VENTA AL CONTADO
                    salesList = em.createNativeQuery("select a.cod_art, sum(a.cantidad),  (sum(a.cantidad) * a.cu) " +
                        "FROM articulos_pedido a " +
                        "JOIN ventadirecta v 	ON a.idventadirecta = v.idventadirecta " +
                        "WHERE v.fecha_pedido BETWEEN :startDate AND :endDate " +
                        "AND v.estado <> 'ANULADO' " +
                        "AND v.idusuario = 5 " +
                        "GROUP BY a.cod_art")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();*/

                produtTypeMessage = MessageUtils.getMessage(ProductSaleType.VETERINARY_PRODUCT.getResourceKey());
            }

        } catch (NoResultException e) {}

        String periodMessage = Month.getMonth(startDate).getMonthLiteral() + "/" + DateUtils.getCurrentYear(startDate);
        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "Costo de ventas a credito " + produtTypeMessage + " " + periodMessage +" Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy"));
        voucher.setDocumentType(Constants.CV_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        /*if (sales.size() > 0) {
            for (CustomerOrder sale : sales) {
                for (ArticleOrder articleOrder : sale.getArticulosPedidos())
                    totalCost = BigDecimalUtil.sum(totalCost, BigDecimalUtil.multiply(articleOrder.getCu(), BigDecimalUtil.toBigDecimal(articleOrder.getAmount())));
                sale.setCvFlag(true);
            }

            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                    ctaCost,
                    totalCost,
                    FinancesCurrencyType.P, null));

            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                    ctaAlm,
                    totalCost,
                    FinancesCurrencyType.P, null));

            voucher.setDate(endDate);
            voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
            saveVoucher(voucher);
        }*/

        BigDecimal sumCost = BigDecimal.ZERO;

        for (Object[] sale : salesList){ sumCost = BigDecimalUtil.sum(sumCost, (BigDecimal) sale[2], 6); }

        VoucherDetail voucherDebit = new VoucherDetail();
        voucherDebit.setAccount(ctaCost.getAccountCode());
        voucherDebit.setDebit(BigDecimalUtil.roundBigDecimal(sumCost,2));
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);
        voucher.addVoucherDetail(voucherDebit);

        for (Object[] sale : salesList){
            String codArt       = (String)sale[0];
            BigDecimal quantity = (BigDecimal) sale[1];
            BigDecimal cost     = (BigDecimal) sale[2];

            VoucherDetail voucherCredit = new VoucherDetail();
            voucherCredit.setAccount(ctaAlm.getAccountCode());
            voucherCredit.setDebit(BigDecimal.ZERO);
            voucherCredit.setCredit(BigDecimalUtil.roundBigDecimal(cost,2));

            voucherCredit.setProductItemCode(codArt);
            voucherCredit.setQuantityArt(quantity);

            voucherCredit.setCurrency(FinancesCurrencyType.P);
            voucherCredit.setExchangeAmount(BigDecimal.ONE);
            voucherCredit.setDebitMe(BigDecimal.ZERO);
            voucherCredit.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredit);
        }
        saveVoucher(voucher);
    }

    public Double calculateCashTransferAmount(Date startDate, Date endDate){

        Double result = (Double) em.createNamedQuery("CashSale.calculateCashTransferAmount")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("flagct", Boolean.FALSE)
                .getSingleResult();
        System.out.println("===================> start: " + startDate.toString() + " - " + result);
        System.out.println("===================>   end: " + endDate.toString() + " - " + result);

        if (result == null)
            result = new Double(0);

        return  result;
    }

    public Double calculateCashTransferAmountFromCustomerOrder(Date startDate, Date endDate){

        /** MODIFYID **/
        Double result = (Double) em.createQuery (
                "select sum(c.totalAmount) from CustomerOrder c " +
                "where c.orderDate between :startDate and :endDate " +
                "and c.saleType = :saleType " +
                "and c.state <> :state " +
                "and c.user.id=408 " +
                "and c.flagct = :flagct ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("saleType", SaleTypeEnum.CASH)
                .setParameter("state", SaleStatus.ANULADO)
                .setParameter("flagct", Boolean.FALSE)
                .getSingleResult();

        System.out.println("===================> start: " + startDate.toString() + " - " + result);
        System.out.println("===================>   end: " + endDate.toString() + " - " + result);

        if (result == null)
            result = new Double(0);

        return  result;
    }

    public void generateCashTransferAccountEntry(Date startDate, Date endDate, Double transferAmount, String gloss) throws CompanyConfigurationNotFoundException{

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        List<CashSale> cashSaleList = em.createNamedQuery("CashSale.findBetweenDates")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("flagct", Boolean.FALSE)
                .getResultList();

        List<CustomerOrder> customerOrderList = em.createNamedQuery("CustomerOrder.findBetweenDates")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("flagct", Boolean.FALSE)
                .getResultList();

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
        voucher.setDocumentType(Constants.CT_VOUCHER_DOCTYPE);

        DateIterator iDate = new DateIterator(startDate, endDate);
        while (iDate.hasNext()) {
            Double amount = calculateCashTransferAmount(iDate.getCurrent(), iDate.getCurrent());
            Double amount2 = calculateCashTransferAmountFromCustomerOrder(iDate.getCurrent(), iDate.getCurrent());
            if (amount > 0){
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                        companyConfiguration.getSavingsBankAccount(),
                        BigDecimalUtil.toBigDecimal(amount),
                        FinancesCurrencyType.P, null));
            }
            if (amount2 > 0){
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                        companyConfiguration.getSavingsBankAccount(),
                        BigDecimalUtil.toBigDecimal(amount2),
                        FinancesCurrencyType.P, null));
            }
            iDate.next();
        }

        voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getVeterinaryCashAccount(),
                BigDecimalUtil.toBigDecimal(transferAmount),
                FinancesCurrencyType.P, null));


        for (CashSale cashSale:cashSaleList){
            cashSale.setFlagct(true);
            em.flush();
        }

        for (CustomerOrder customerOrder:customerOrderList){
            customerOrder.setFlagct(true);
            em.flush();
        }

        voucher.setDate(endDate);
        voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
        saveVoucher(voucher);

        /*if (transferAmount > 0){
            Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
            voucher.setDocumentType(Constants.CT_VOUCHER_DOCTYPE);

            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                    companyConfiguration.getSavingsBankAccount(),
                    BigDecimalUtil.toBigDecimal(transferAmount),
                    FinancesCurrencyType.P, null));

            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                    companyConfiguration.getVeterinaryCashAccount(),
                    BigDecimalUtil.toBigDecimal(transferAmount),
                    FinancesCurrencyType.P, null));

            voucher.setDate(endDate);
            voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
            saveVoucher(voucher);
        }*/

    }

    @Override
    public DocType getDocType(String name) {

        DocType docType = (DocType)em.createNamedQuery("DocType.findDocumentByName").setParameter("name", name).getSingleResult();

        return docType;
    }

    public List<Object[]> getSumsClosingResults(Date startDate, Date endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        datas = em.createQuery(
                " SELECT " +
                        " cashAccount.accountCode, " +
                        " cashAccount.description, " +
                        " SUM(voucherDetail.debit) AS debit, " +
                        " SUM(voucherDetail.credit) AS credit" +
                        " FROM VoucherDetail voucherDetail " +
                        " LEFT JOIN voucherDetail.voucher voucher " +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                        " WHERE voucher.state <> 'ANL' " +
                        " AND voucher.date between :startDate and :endDate " +
                        " AND (cashAccount.accountType =:typeE OR cashAccount.accountType =:typeI) " +
                        " GROUP BY cashAccount.accountCode, cashAccount.description ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("typeE", CashAccountType.E)
                .setParameter("typeI", CashAccountType.I)
                .getResultList();

        System.out.println("----------SUMAS-Y-SALDOS-------");
        for(Object[] obj: datas){
            System.out.println(obj[0] + " - " + obj[2] + " - " + obj[3] + " - " + obj[1] );
        }

        return datas;
    }

    public List<Object[]> getSumsBalanceClosure(Date startDate, Date endDate){

        List<Object[]> datas = new ArrayList<Object[]>();

        datas = em.createQuery(
                " SELECT " +
                        " cashAccount.accountCode, " +
                        " cashAccount.description, " +
                        " SUM(voucherDetail.debit) AS debit, " +
                        " SUM(voucherDetail.credit) AS credit" +
                        " FROM VoucherDetail voucherDetail " +
                        " LEFT JOIN voucherDetail.voucher voucher " +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                        " WHERE voucher.state <> 'ANL' " +
                        " AND voucher.date between :startDate and :endDate " +
                        " AND (cashAccount.accountType =:typeA OR " +
                              "cashAccount.accountType =:typeP OR " +
                              "cashAccount.accountType =:typeC OR " +
                              "cashAccount.accountType =:typeOD OR " +
                              "cashAccount.accountType =:typeOA) " +
                        " GROUP BY cashAccount.accountCode, cashAccount.description ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("typeA", CashAccountType.A)
                .setParameter("typeP", CashAccountType.P)
                .setParameter("typeC", CashAccountType.C)
                .setParameter("typeOD", CashAccountType.OD)
                .setParameter("typeOA", CashAccountType.OA)
                .getResultList();

        System.out.println("----------SUMAS-Y-SALDOS-BALANCE------");
        for(Object[] obj: datas){
            System.out.println(obj[0] + " - " + obj[2] + " - " + obj[3] + " - " + obj[1] );
        }

        return datas;
    }

    public Integer getNextMaxNumberByDocType(String docType, Date startDate, Date endDate){

        BigDecimal number = (BigDecimal) em.createNativeQuery("SELECT MAX(CAST(no_doc AS DECIMAL)) FROM sf_tmpenc " +
                "WHERE tipo_doc =:docType " +
                "AND fecha between :startDate and :endDate ")
                .setParameter("docType", docType)
                .setParameter("startDate",startDate)
                .setParameter("endDate",endDate)
                .getSingleResult();

        Integer result = new Integer(number.toString());
        result++;
        return result;
    }

    public List<Object[]> getValuedInventory(Date startDate, Date endDate, CashAccount cashAccount){

        List<Object[]> datas = new ArrayList<Object[]>();

        datas = em.createNativeQuery("" +
                "SELECT d.cod_art, a.descri, a.cod_med, " +
                "SUM(d.debe)     AS debe, " +
                "SUM(d.haber)    AS haber, " +
                "SUM(IF(d.debe>0, d.cant_art, 0))  AS cant_e, " +
                "SUM(IF(d.haber>0, d.cant_art, 0)) AS cant_s " +
                "FROM sf_tmpdet d " +
                "LEFT JOIN sf_tmpenc e ON d.id_tmpenc = e.id_tmpenc " +
                "LEFT JOIN inv_articulos a ON d.cod_art = a.cod_art " +
                "WHERE d.cuenta = :cashAccount " +
                "AND e.fecha BETWEEN :startDate AND :endDate " +
                "AND e.estado <> 'ANL' " +
                "GROUP BY d.cod_art, a.descri, a.cod_med order by a.descri asc")
        .setParameter("cashAccount", cashAccount.getAccountCode())
        .setParameter("startDate", startDate)
        .setParameter("endDate", endDate).getResultList();

        return datas;

    }

    public List<Object[]> getProductionCostAccountResults(Date startDate, Date endDate, CashAccount cashAccountProductionCost){

        List<Object[]> datas = new ArrayList<Object[]>();

        datas = em.createQuery(
                " SELECT " +
                        " cashAccount.accountCode, " +
                        " cashAccount.description, " +
                        " SUM(voucherDetail.debit) AS debit, " +
                        " SUM(voucherDetail.credit) AS credit" +
                        " FROM VoucherDetail voucherDetail " +
                        " LEFT JOIN voucherDetail.voucher voucher " +
                        " LEFT JOIN voucherDetail.cashAccount cashAccount" +
                        " WHERE voucher.state <> 'ANL' " +
                        " AND voucher.date between :startDate and :endDate " +
                        " AND cashAccount.accountType =:typeE " +
                        " AND cashAccount.accountLevel3Code =:accountLevel3Code " +
                        " GROUP BY cashAccount.accountCode, cashAccount.description ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("typeE", CashAccountType.E)
                .setParameter("accountLevel3Code", cashAccountProductionCost.getAccountCode()) /** MODIFYID **/
                .getResultList();

        return datas;
    }

}
