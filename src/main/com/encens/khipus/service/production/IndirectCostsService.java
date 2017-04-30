package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.SubGroup;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface IndirectCostsService extends GenericService {

    public Double getTotalCostIndirectByGroup(SubGroup subGroup);

    public Double getTotalCostIndirectGeneral();

    public Double getCostTotalIndirect(ProductionOrder productionOrder, Double totalVolumDay, Double totalVolumGeneralDay);

    public List<IndirectCosts> getCostTotalIndirect(Date dateConcurrent, int totalDaysNotProducer,ProductionOrder productionOrder, Double totalVolumDay, Double totalVolumGeneralDay, PeriodIndirectCost indirectCost);

    public List<IndirectCosts> getCostTotalIndirectSingle(Date dateConcurrent, int totalDaysNotProducer,SingleProduct single, Double totalVolumDay, Double totalVolumGeneralDay, PeriodIndirectCost periodIndirectCost);

    public PeriodIndirectCost getLastPeroidIndirectCost();

    public int calculateCantDaysProducer(Date date);

    public PeriodIndirectCost getConcurrentPeroidIndirectCost(Date dateConcurrent);

    public List<IndirectCosts> getIndirectCostGeneral(PeriodIndirectCost indirectCost);

    boolean findPeriodIndirectCostUsed(PeriodIndirectCost periodIndirectCost);
}
