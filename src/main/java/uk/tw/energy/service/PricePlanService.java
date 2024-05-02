package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.strategy.CostCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricePlanService {

    private final List<PricePlan> pricePlans;
    private final MeterReadingService meterReadingService;

    private final CostCalculationStrategy costCalculationStrategy;

    public PricePlanService(List<PricePlan> pricePlans, MeterReadingService meterReadingService, CostCalculationStrategy strategy) {
        this.pricePlans = pricePlans;
        this.meterReadingService = meterReadingService;
        this.costCalculationStrategy = strategy;
    }

    public Optional<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachPricePlan(String smartMeterId) {
        Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);

        if (!electricityReadings.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(pricePlans.stream().collect(
                Collectors.toMap(PricePlan::getPlanName, t -> this.costCalculationStrategy.calculateCost(electricityReadings.get(), t))));
    }

    public Optional<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachUser(String smartMeterId, Integer day) {
        Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);

        if (!electricityReadings.isPresent()) {
            return Optional.empty();
        }

        //filter by day parameter
        List<ElectricityReading> items = electricityReadings.get();
        Instant lastDays = Instant.now().minus(day, ChronoUnit.DAYS);
        List<ElectricityReading> readings = items.stream().filter(reading -> reading.time().isAfter(lastDays)).collect(Collectors.toList());

        BigDecimal average = calculateAverageReading(readings);
        BigDecimal timeElapsed = calculateTimeElapsed(readings);
        BigDecimal energyConsumed = average.multiply(timeElapsed);
        BigDecimal cost = BigDecimal.TEN;

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("averageReading", average);
        result.put("usageTime", timeElapsed);
        result.put("energyConsumed", energyConsumed);
        result.put("cost", cost);

        return Optional.of(result);
    }

    public BigDecimal calculateCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
        BigDecimal average = calculateAverageReading(electricityReadings);
        BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);

        BigDecimal averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP);
        return averagedCost.multiply(pricePlan.getUnitRate());
    }

    private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
        BigDecimal summedReadings = electricityReadings.stream()
                .map(ElectricityReading::reading)
                .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));

        return summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
        ElectricityReading first = electricityReadings.stream()
                .min(Comparator.comparing(ElectricityReading::time))
                .get();

        ElectricityReading last = electricityReadings.stream()
                .max(Comparator.comparing(ElectricityReading::time))
                .get();

        return BigDecimal.valueOf(Duration.between(first.time(), last.time()).getSeconds() / 3600.0);
    }
}
