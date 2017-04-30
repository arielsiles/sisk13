package com.encens.khipus.action.purchases.reports;

import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.service.finances.DiscountCommentService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.util.Date;
import java.util.List;

/**
 * Scriptlet to get the DiscountComment and InvoiceOpenAmount associated to the PurchaseOrder
 *
 * @author
 * @version 3.0
 */
public class PurchaseOrderReportScriptlet extends JRDefaultScriptlet {
    private DiscountCommentService discountCommentService = (DiscountCommentService) Component.getInstance("discountCommentService");
    private PurchaseDocumentService purchaseDocumentService = (PurchaseDocumentService) Component.getInstance("purchaseDocumentService");

    @Override
    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        PurchaseOrder purchaseOrder = (PurchaseOrder) getFieldValue("purchaseOrder");
        List<Object[]> discountCommentCauseList = discountCommentService.findCauseByPurchaseOrderId(purchaseOrder.getId());
        this.setVariableValue("cause", concatLineSeparated(discountCommentCauseList));
        this.setVariableValue("purchaseDocumentOpenAmount", purchaseDocumentService.getPurchaseDocumentOpenAmount(purchaseOrder));
    }

    /**
     * Concat a List of Object[] of Date and Strings with internal space separation
     * and \n\n separation from each list element
     *
     * @param discountCommentCauseList a list of Strings
     * @return a String concatenated and separated by \n\n
     */
    @SuppressWarnings({"unchecked"})
    public static String concatLineSeparated(List<Object[]> discountCommentCauseList) {
        String result = "";
        for (Object[] discountComment : discountCommentCauseList) {
            result += FormatUtils.concat(DateUtils.format((Date) discountComment[1], MessageUtils.getMessage("patterns.date")), discountComment[0]) + "\n\n";
        }
        if (discountCommentCauseList.size() > 0) {
            result = result.substring(0, result.lastIndexOf("\n\n"));
        }
        return result;
    }

}
