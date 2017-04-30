package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class HumanResourcesExpenseExtendedInstanceFactory implements InstanceFactory<HumanResourcesExpenseExtended> {
    public HumanResourcesExpenseExtended createInstance(HumanResourcesExpenseExtended cachedObject, Object[] row) {
        HumanResourcesExpenseExtended instance = new HumanResourcesExpenseExtended();
        instance.setCode(row[0].toString());
        instance.setDescription(row[1].toString());

        instance.setJanuaryBs((BigDecimal) row[2]);
        instance.setJanuaryUsd((BigDecimal) row[3]);
        instance.setTotalJanuaryUsd((BigDecimal) row[5]);

        instance.setFebruaryBs((BigDecimal) row[6]);
        instance.setFebruaryUsd((BigDecimal) row[7]);
        instance.setTotalFebruaryUsd((BigDecimal) row[9]);

        instance.setMarchBs((BigDecimal) row[10]);
        instance.setMarchUsd((BigDecimal) row[11]);
        instance.setTotalMarchUsd((BigDecimal) row[13]);

        instance.setAprilBs((BigDecimal) row[14]);
        instance.setAprilUsd((BigDecimal) row[15]);
        instance.setTotalAprilUsd((BigDecimal) row[17]);

        instance.setMayBs((BigDecimal) row[18]);
        instance.setMayUsd((BigDecimal) row[19]);
        instance.setTotalMayUsd((BigDecimal) row[21]);

        instance.setJuneBs((BigDecimal) row[22]);
        instance.setJuneUsd((BigDecimal) row[23]);
        instance.setTotalJuneUsd((BigDecimal) row[25]);

        instance.setJulyBs((BigDecimal) row[26]);
        instance.setJulyUsd((BigDecimal) row[27]);
        instance.setTotalJulyUsd((BigDecimal) row[29]);

        instance.setAugustBs((BigDecimal) row[30]);
        instance.setAugustUsd((BigDecimal) row[31]);
        instance.setTotalAugustUsd((BigDecimal) row[33]);

        instance.setSeptemberBs((BigDecimal) row[34]);
        instance.setSeptemberUsd((BigDecimal) row[35]);
        instance.setTotalSeptemberUsd((BigDecimal) row[37]);

        instance.setOctoberBs((BigDecimal) row[38]);
        instance.setOctoberUsd((BigDecimal) row[39]);
        instance.setTotalOctoberUsd((BigDecimal) row[41]);

        instance.setNovemberBs((BigDecimal) row[42]);
        instance.setNovemberUsd((BigDecimal) row[43]);
        instance.setTotalNovemberUsd((BigDecimal) row[45]);

        instance.setDecemberBs((BigDecimal) row[46]);
        instance.setDecemberUsd((BigDecimal) row[47]);
        instance.setTotalDecemberUsd((BigDecimal) row[49]);

        instance.setBs((BigDecimal) row[50]);
        instance.setUsd((BigDecimal) row[51]);
        instance.setTotalUsd((BigDecimal) row[53]);

        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
