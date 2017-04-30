package com.encens.khipus.util;

import com.encens.khipus.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
public final class ListUtil {
    public static final ListUtil i = new ListUtil();

    private ListUtil() {

    }

    public <T> List<List<T>> partition(List<T> list, Integer limit) {
        List<List<T>> result = new ArrayList<List<T>>();

        if (0 >= limit) {
            throw new IllegalArgumentException("The limit should be a positive Integer number.");
        }

        Integer listSize = list.size();
        if (0 == listSize) {
            return result;
        }

        Integer rangeCounter = calculateRanges(list.size(), limit);

        Integer firstIdx = 0;
        Integer lastIdx = limit;

        for (int i = 0; i < rangeCounter; i++) {
            if (i == 0) {
                if (listSize - 1 < lastIdx) {
                    lastIdx = listSize;
                }
            } else {
                firstIdx = lastIdx;
                lastIdx = (i + 1) * (limit);
                if (listSize - 1 < lastIdx) {
                    lastIdx = listSize;
                }
            }

            result.add(list.subList(firstIdx, lastIdx));
        }

        return result;
    }

    private Integer calculateRanges(Integer listSize, Integer limit) {
        int counter = 1;

        while (listSize > limit) {
            counter++;
            listSize -= limit;
        }

        return counter;
    }

    public <T extends BaseModel> List getIdList(List<T> baseModelList) {
        ArrayList result = new ArrayList();
        for (T instance : baseModelList) {
            result.add(instance.getId());
        }
        return result;
    }

    public <T> T getLastElement(List<T> list) {
        return !ValidatorUtil.isEmptyOrNull(list) ? list.get(list.size() - 1) : null;
    }
}
