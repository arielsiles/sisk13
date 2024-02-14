package com.encens.khipus.service.production;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.FinanceProviderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;

@Stateless
@Name("collectMaterialService")
@AutoCreate
public class CollectMaterialServiceBean implements CollectMaterialService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private FinanceProviderService financeProviderService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private FacesMessages facesMessages;

    @Override
    public List<CollectMaterial> findCollectMaterialNoAccounting(Date startDate, Date endDate) {
        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                        "where c.date between :startDate and :endDate and c.state != 'PEN' and c.accountigFlag = :state")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .setParameter("state", Boolean.FALSE)
                .getResultList();

        return resultList;
    }

    @Override
    public List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate) {
        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                        "where c.date between :startDate and :endDate and c.state = :state")
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .setParameter("state", CollectMaterialState.APR)
                .getResultList();

        return resultList;
    }

    @Override
    public List<CollectMaterial> findApprovedCollectMaterialByCode(String productItemCode, Date startDate, Date endDate) {

        List<CollectMaterial> resultList = em.createQuery("select c from CollectMaterial c " +
                "where c.date between :startDate and :endDate and c.metaProduct.productItemCode = :productItemCode and c.state = :state")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .setParameter("state", CollectMaterialState.APR)
                .getResultList();

        return resultList;
    }

    @Override
    public String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList , Date startDate, Date endDate) {
        String result = Outcome.FAIL;
        String gloss = "";
        if ( startDate.compareTo(endDate) == 0 )
            gloss = "INGRESO ALMACEN DE MATERIAS PRIMAS DEL " + DateUtils.format(startDate, "dd/MM/yyyy");
        else
            gloss = "INGRESO ALMACEN DE MATERIAS PRIMAS DEL " + DateUtils.format(startDate, "dd/MM/yyyy") + " AL " + DateUtils.format(endDate, "dd/MM/yyyy");

        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, gloss);
        voucher.setDocumentType(Constants.IA_VOUCHER_DOCTYPE);

        List<VoucherDetail> supplierDetailCashAcounts = new ArrayList<VoucherDetail>();
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        FinancesEntity financesEntity;

        for (CollectMaterial colMat:collectMaterialList) {
            financesEntity = financeProviderService.findByIdNumber(colMat.getProducer().getIdNumber());

            CashAccount warehouseCashAccount = colMat.getMetaProduct().getProductItem().getWarehouse().getWarehouseCashAccount();
            BigDecimal aux = BigDecimalUtil.divide(colMat.getBalanceWeight(),BigDecimalUtil.toBigDecimal(1000));
            BigDecimal amount = BigDecimalUtil.multiply(aux, colMat.getPrice());

            VoucherDetail voucherDetailDev = VoucherDetailBuilder.newDebitVoucherDetail(
                    null, null, warehouseCashAccount, amount, FinancesCurrencyType.P, BigDecimal.ONE);

            voucherDetailDev.setQuantityArt(colMat.getBalanceWeight());
            voucherDetailDev.setProductItemCode(colMat.getMetaProduct().getProductItem().getProductItemCode());
            voucher.getDetails().add(voucherDetailDev);

            VoucherDetail supplierAccountOutput = VoucherDetailBuilder.newCreditVoucherDetail(
                    null, null, companyConfiguration.getAccountPayableSupplier(), amount, FinancesCurrencyType.P, BigDecimal.ONE);

            //voucher.getDetails().add(supplierAccountOutput);

            supplierAccountOutput.setProviderCode(financesEntity.getId().toString());
            supplierDetailCashAcounts.add(supplierAccountOutput);
            //voucher.getDetails().add(voucherDetailDev);

        }

        Collections.sort(supplierDetailCashAcounts, new Comparator<VoucherDetail>() {
            @Override
            public int compare(VoucherDetail o1, VoucherDetail o2) {
                return o1.getAccount().compareTo(o2.getAccount());
            }
        });

        for (VoucherDetail voucherDetail:supplierDetailCashAcounts){
            voucher.getDetails().add(voucherDetail);
        }

        voucher.setDate(new Date());
        voucherAccoutingService.saveVoucher(voucher);
        result = Outcome.SUCCESS;
        return  result;
    }

    public CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
    }

}
