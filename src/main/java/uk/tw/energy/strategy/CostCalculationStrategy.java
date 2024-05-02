package uk.tw.energy.strategy;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.util.List;

public interface CostCalculationStrategy {
    BigDecimal calculateCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan);
}
