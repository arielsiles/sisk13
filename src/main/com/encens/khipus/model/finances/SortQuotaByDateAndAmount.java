package com.encens.khipus.model.finances;

import java.util.Comparator;

/**
 * Implementation of comparator for Quota by Expiration Date and Amount
 *
 * @author
 * @version 2.26
 */
public class SortQuotaByDateAndAmount implements Comparator<Quota> {

    @Override
    public int compare(Quota o1, Quota o2) {
        int result = o1.getExpirationDate().compareTo(o2.getExpirationDate());
        if (result == 0) {
            result = o1.getAmount().compareTo(o2.getAmount());
        }
        return result;
    }
}
