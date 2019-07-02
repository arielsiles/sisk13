package com.encens.khipus.util.employees.payroll.tributary;

import com.encens.khipus.model.employees.BonusType;
import com.encens.khipus.model.employees.CategoryTributaryPayroll;
import com.encens.khipus.model.employees.GrantedBonus;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 0
 */
public class GeneralBonusCalculator extends Calculator<CategoryTributaryPayroll> {
    private List<GrantedBonus> grantedBonus;

    public GeneralBonusCalculator(List<GrantedBonus> grantedBonus) {
        this.grantedBonus = grantedBonus;
    }

    @Override
    public void execute(CategoryTributaryPayroll instance) {
        instance.setSundayBonus(BigDecimal.ZERO);
        instance.setProductionBonus(BigDecimal.ZERO);

        BigDecimal otherBonus = BigDecimal.ZERO;
        BigDecimal sundayBonus = BigDecimal.ZERO;
        BigDecimal productionBonus = BigDecimal.ZERO;

        BigDecimal nightWork = BigDecimal.ZERO;
        BigDecimal transportationReturn = BigDecimal.ZERO;
        BigDecimal refreshment = BigDecimal.ZERO;

        if (null != grantedBonus) {
            for (GrantedBonus element : grantedBonus) {
                BonusType bonusType = element.getBonus().getBonusType();

                if (BonusType.REGULAR_BONUS.equals(bonusType)) {
                    otherBonus = BigDecimalUtil.sum(otherBonus, element.getAmount());
                }

                if (BonusType.SUNDAYS_BONUS.equals(bonusType)) {
                    sundayBonus = BigDecimalUtil.sum(sundayBonus, element.getAmount());
                }

                if (BonusType.PRODUCTION_BONUS.equals(bonusType)) {
                    productionBonus = BigDecimalUtil.sum(productionBonus, element.getAmount());
                }

                if (BonusType.NIGHTWORK_BONUS.equals(bonusType)) {
                    nightWork = BigDecimalUtil.sum(nightWork, element.getAmount());
                }

                if (BonusType.TRANSRETURN_BONUS.equals(bonusType)) {
                    transportationReturn = BigDecimalUtil.sum(transportationReturn, element.getAmount());
                }

                if (BonusType.REFRESHMENT_BONUS.equals(bonusType)) {
                    refreshment = BigDecimalUtil.sum(refreshment, element.getAmount());
                }
            }
        }

        instance.setOtherBonus(otherBonus);
        instance.setSundayBonus(sundayBonus);
        instance.setProductionBonus(productionBonus);

        instance.setNightWorkBonus(nightWork);
        instance.setTransReturnBonus(transportationReturn);
        instance.setRefreshmentBonus(refreshment);

    }
}
