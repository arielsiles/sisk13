package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.PeriodType;
import com.encens.khipus.model.finances.Quota;
import com.encens.khipus.model.finances.QuotaState;
import com.encens.khipus.model.finances.SortQuotaByDateAndAmount;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.*;


/**
 * Action to allow the creation and edit by a grid of the quotas associated to the Rotatory Fund
 *
 * @author
 * @version 2.26
 */
@Name("rotatoryFundCreateQuotaListAction")
@Scope(ScopeType.CONVERSATION)
public class RotatoryFundCreateQuotaListAction extends GenericAction<Quota> {

    private List<Quota> quotaList = new ArrayList<Quota>();
    /* the main action which will control the persistence of this list*/
    @In
    private RotatoryFundAction rotatoryFundAction;
    /*holds the sum of amounts*/
    private BigDecimal quotaSum = BigDecimal.ZERO;
    @In
    private FacesMessages facesMessages;


    /* restart the quota list according to the parameters specified by the RotatoryFund */

    public void reset() {
        quotaList.clear();
        if (null != rotatoryFundAction.getInstance().getAmount()) {
            Date rotatoryFundDate = new Date(rotatoryFundAction.getInstance().getStartDate().getTime());
            Calendar originalRotatoryFundCalendar = DateUtils.toCalendar(rotatoryFundDate);
            Calendar rotatoryFundCalendar = DateUtils.toCalendar(rotatoryFundDate);
            int dayOfMonth = originalRotatoryFundCalendar.get(Calendar.DAY_OF_MONTH);

            Double quotaAmount = (rotatoryFundAction.getInstance().getAmount().doubleValue() / rotatoryFundAction.getInstance().getPaymentsNumber());
            /*round to 2 decimals*/
            quotaAmount = BigDecimalUtil.toBigDecimal(quotaAmount).doubleValue();
            quotaSum = rotatoryFundAction.getInstance().getAmount();
            for (int i = 0; i < rotatoryFundAction.getInstance().getPaymentsNumber(); i++) {
                /*create the quotas*/
                Quota quota = new Quota();
                /* fill the information necessary for the new quota object*/
                fillQuotaInfo(rotatoryFundCalendar, quotaAmount, quota);

                /*Update the expiration date of the rotatoryFund*/
                if (i == rotatoryFundAction.getInstance().getPaymentsNumber() - 1) {
                    rotatoryFundAction.getInstance().setExpirationDate(quota.getExpirationDate());
                    double difference = rotatoryFundAction.getInstance().getAmount().doubleValue() - (quotaAmount * (rotatoryFundAction.getInstance().getPaymentsNumber() - 1));
                    quota.setAmount(BigDecimalUtil.toBigDecimal(difference));
                }
                /*move to the next date a step*/
                stepDate(rotatoryFundCalendar, dayOfMonth);
                quotaList.add(quota);
            }
            orderQuotaList();
        }
    }

    /*order the element of the list by expiration date and amount */

    public void orderQuotaList() {
        /*order by expiration date and amount*/
        Collections.sort(quotaList, new SortQuotaByDateAndAmount());
    }

    /* fill the information necessary for the new quota object*/

    private void fillQuotaInfo(Calendar rotatoryFundCalendar, Double quotaAmount, Quota quota) {
        quota.setRotatoryFund(rotatoryFundAction.getInstance());
        quota.setState(QuotaState.PEN);
        quota.setAmount(BigDecimalUtil.toBigDecimal(quotaAmount));
        quota.setResidue(quota.getAmount());
        quota.setExpirationDate(new Date(rotatoryFundCalendar.getTime().getTime()));
    }

    private void stepDate(Calendar rotatoryFundCalendar, int dayOfMonth) {
        if (rotatoryFundAction.getPeriodType().equals(PeriodType.MONTH)) {
            rotatoryFundCalendar.add(Calendar.MONTH, rotatoryFundAction.getInterval());
            int dayOfCurrentMonth = rotatoryFundCalendar.get(Calendar.DAY_OF_MONTH);
            int maxDayOfMonth = rotatoryFundCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfCurrentMonth < dayOfMonth
                    && dayOfCurrentMonth < maxDayOfMonth) {
                rotatoryFundCalendar.add(Calendar.DATE, ((Math.min(maxDayOfMonth, dayOfMonth)) - dayOfCurrentMonth));
            }
        }

        if (rotatoryFundAction.getPeriodType().equals(PeriodType.DAY)) {
            rotatoryFundCalendar.add(Calendar.DAY_OF_MONTH, rotatoryFundAction.getInterval());
        }
    }

    /*  add a quota element and sort the quota list*/

    public void addQuota() {
        if (null != rotatoryFundAction.getInstance().getAmount()) {
            Date rotatoryFundDate = new Date(rotatoryFundAction.getInstance().getStartDate().getTime());
            Calendar originalRotatoryFundCalendar = DateUtils.toCalendar(rotatoryFundDate);
            int dayOfMonth = originalRotatoryFundCalendar.get(Calendar.DAY_OF_MONTH);
            Calendar maxQuotaCalendar = DateUtils.toCalendar(quotaList.get(quotaList.size() - 1).getExpirationDate());
            stepDate(maxQuotaCalendar, dayOfMonth);
            rotatoryFundAction.getInstance().setPaymentsNumber(rotatoryFundAction.getInstance().getPaymentsNumber() + 1);
            BigDecimal difference;

            Quota quota = new Quota();
            if (rotatoryFundAction.isRedistributePayment()) {
                BigDecimal quotaAmount = BigDecimalUtil.divide(rotatoryFundAction.getInstance().getAmount(),
                        BigDecimalUtil.toBigDecimal(rotatoryFundAction.getInstance().getPaymentsNumber()));
                quotaSum = rotatoryFundAction.getInstance().getAmount();
                /*update the old quotas*/
                for (Quota oldQuota : getQuotaList()) {
                    oldQuota.setAmount(quotaAmount);
                    oldQuota.setResidue(oldQuota.getAmount());
                }

                /*the difference may be equal or greater than zero*/
                difference = BigDecimalUtil.subtract(rotatoryFundAction.getInstance().getAmount(),
                        BigDecimalUtil.multiply(quotaAmount, BigDecimalUtil.toBigDecimal(getQuotaList().size())));
            } else {
                /*update quota sum*/
                quotaSum = BigDecimal.ZERO;
                for (Quota quota1 : quotaList) {
                    quotaSum = BigDecimalUtil.sum(quotaSum, quota1.getAmount());
                }
                difference = BigDecimalUtil.subtract(rotatoryFundAction.getInstance().getAmount(), quotaSum);
                if (difference.compareTo(BigDecimal.ZERO) <= 0) {
                    difference = BigDecimal.ZERO;
                }
            }
            fillQuotaInfo(maxQuotaCalendar, difference.doubleValue(), quota);
            rotatoryFundAction.getInstance().setExpirationDate(quota.getExpirationDate());
            quotaList.add(quota);
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFund.error.toAddQuotaAmountFieldMustBeSpecified");
        }
    }

    public void removeQuota(Quota quota) {
        if (quotaList.size() > 1) {
            quotaList.remove(quota);

            rotatoryFundAction.getInstance().setPaymentsNumber(rotatoryFundAction.getInstance().getPaymentsNumber() - 1);
            BigDecimal difference;
            if (rotatoryFundAction.isRedistributePayment()) {
                BigDecimal quotaAmount = BigDecimalUtil.divide(rotatoryFundAction.getInstance().getAmount(),
                        BigDecimalUtil.toBigDecimal(rotatoryFundAction.getInstance().getPaymentsNumber()));
                /*update the old quotas*/
                for (Quota oldQuota : getQuotaList()) {
                    oldQuota.setAmount(quotaAmount);
                    oldQuota.setResidue(oldQuota.getAmount());
                }

                /*the difference may be equal or greater than zero*/
                difference = BigDecimalUtil.subtract(rotatoryFundAction.getInstance().getAmount(),
                        BigDecimalUtil.multiply(quotaAmount, BigDecimalUtil.toBigDecimal(getQuotaList().size() - 1)));

                getLastQuota().setAmount(difference);
                getLastQuota().setResidue(difference);

            }

        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "RotatoryFund.error.paymentsNumberCannotBeLessThanOne");
        }
        rotatoryFundAction.getInstance().setExpirationDate(getLastQuota().getExpirationDate());
        rotatoryFundAction.getInstance().setStartDate(getFirstQuota().getExpirationDate());
    }

    public void quotaAmountChanged(Quota quota) {
        orderQuotaList();
    }

    public void expirationDateChanged() {
        orderQuotaList();
        rotatoryFundAction.getInstance().setExpirationDate(getLastQuota().getExpirationDate());
        rotatoryFundAction.getInstance().setStartDate(getFirstQuota().getExpirationDate());
        rotatoryFundAction.setStartDate(rotatoryFundAction.getInstance().getStartDate());
    }

    public void rotatoryFundStartDateChanged() {
        Long dateDelta = DateUtils.daysBetween(rotatoryFundAction.getStartDate(), rotatoryFundAction.getInstance().getStartDate(), false);
        log.debug("days to move" + dateDelta);
        for (Quota quota : quotaList) {
            Calendar newDate = Calendar.getInstance();
            newDate.setTime(quota.getExpirationDate());
            newDate.add(Calendar.DATE, dateDelta.intValue());
            quota.setExpirationDate(newDate.getTime());
        }
        rotatoryFundAction.setStartDate(rotatoryFundAction.getInstance().getStartDate());
        /*last quota may be null at the beginning*/
        if (getLastQuota() != null) {
            rotatoryFundAction.getInstance().setExpirationDate(getLastQuota().getExpirationDate());
        }
        /* here manage old and new start date*/
    }

    public Quota getLastQuota() {
        if (quotaList.size() <= 0) {
            return null;
        } else {
            return quotaList.get((quotaList.size() - 1));
        }
    }

    public Quota getFirstQuota() {
        return quotaList.get(0);
    }

    public List<Quota> getQuotaList() {
        return quotaList;
    }

    public void setQuotaList(List<Quota> quotaList) {
        this.quotaList = quotaList;
    }
}
