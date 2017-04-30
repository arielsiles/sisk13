package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.FinanceUser;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.usertype.StringBooleanUserType;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.finances.VoucherServiceBean;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("voucherEntriesApproveAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherEntriesApproveAction extends GenericAction<Voucher> {

       private BusinessUnit businessUnit;
       private FinancesModule financesModule;
       private FinanceUser financeUser;
       private String numberTransaction;

       @DataModel
       public List<String> getListErrors() {
        return getErrors();
       }
       List<VoucherServiceBean.ObsApprovedEntries> obsApprovedEntrieses = new ArrayList<VoucherServiceBean.ObsApprovedEntries>();

       @In
       private VoucherService voucherService;

       public void approvedAllVoucherEntries(Date startDate, Date endDate,String numberTransaction, AccountEntriesDataModel accountEntriesDataModel ){
           try {
               List<String> transactionNumbers = new ArrayList<String>();
               voucherService.approvedAllVoucherEntries(Constants.defaultCompanyNumber,businessUnit,startDate,endDate,numberTransaction,financeUser,financesModule);
               obsApprovedEntrieses = voucherService.getInfoTrasaction(financesModule,startDate,endDate);

               if(obsApprovedEntrieses.size() == 0)
               {
                   addErrorFailApprovedMessage();
               }else{
                   showObservationMessage(obsApprovedEntrieses);
                   for(VoucherServiceBean.ObsApprovedEntries obs: obsApprovedEntrieses)
                   {
                       transactionNumbers.add(obs.getNumberTransaction());
                   }
                   accountEntriesDataModel.setTransactionsNumbers(transactionNumbers);
                   accountEntriesDataModel.search();
               }

           } catch (CompanyConfigurationNotFoundException e) {
               e.printStackTrace();
           }
       }

    private void showObservationMessage(List<VoucherServiceBean.ObsApprovedEntries> observations) {
        for(VoucherServiceBean.ObsApprovedEntries obs: observations)
        {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"AccountEntries.info.observationsAppoved",obs.getState(),obs.getObservations());
        }
    }

    private void addErrorFailApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"AccountEntries.error.failApproved");
    }

    public BusinessUnit getBusinessUnit() {
            return businessUnit;
        }

        public void setBusinessUnit(BusinessUnit businessUnit) {
            this.businessUnit = businessUnit;
        }

        public FinancesModule getFinancesModule() {
            return financesModule;
        }

        public void setFinancesModule(FinancesModule financesModule) {
            this.financesModule = financesModule;
        }

        public FinanceUser getFinanceUser() {
            return financeUser;
        }

        public void setFinanceUser(FinanceUser financeUser) {
            this.financeUser = financeUser;
        }

        public void cleanFinanceUser(){
            this.financeUser = null;
        }

    public List<String> getErrors() {
        List<String> errors = new ArrayList<String>();
        for(VoucherServiceBean.ObsApprovedEntries obsApprovedEntries: voucherService.getInfoTrasaction(numberTransaction))
        {
          errors.add(obsApprovedEntries.getObservations());
        }
        return errors;
    }

    public String getNumberTransaction() {
        return numberTransaction;
    }

    public void setNumberTransaction(String numberTransaction) {
        this.numberTransaction = numberTransaction;
    }
}
