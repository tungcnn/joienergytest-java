package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UsageCostService {
    private final List<PricePlan> pricePlans;

    public UsageCostService(List<PricePlan> pricePlans) {
        this.pricePlans = pricePlans;
    }

    public BigDecimal getUnitRate(String pricePlanId) {
        ArrayList<PricePlan> pricePlans = (ArrayList<PricePlan>) this.pricePlans;
        for (PricePlan pp:pricePlans) {
            if (pp.getPlanName().equals(pricePlanId)) return pp.getUnitRate();
        }
        return null;
    }

    private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
        BigDecimal summedReadings = electricityReadings.stream()
                .map(ElectricityReading::getReading)
                .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));

        return summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);
    }

    private List<ElectricityReading> getReadingsFromLastWeek(List<ElectricityReading> readings) {
        ElectricityReading last = readings.stream()
                .max(Comparator.comparing(ElectricityReading::getTime))
                .get();
        long lastWeek = last.getTime().getEpochSecond() - 7*24*60*60;
        ArrayList<ElectricityReading> readingArrayList = (ArrayList<ElectricityReading>) readings;
        ArrayList<ElectricityReading> readingsFromLastWeek = new ArrayList<>();
        for (ElectricityReading e:readingArrayList) {
            if(e.getTime().getEpochSecond() > lastWeek) {
                readingsFromLastWeek.add(e);
            }
        }
        return readingsFromLastWeek;
    }

    public BigDecimal calculateUsageCostOfLastWeek(String pricePlanId, List<ElectricityReading> readings) {
        BigDecimal unitRate = getUnitRate(pricePlanId);
        List<ElectricityReading> readingsFromLastWeek = getReadingsFromLastWeek(readings);
        BigDecimal averageReading = calculateAverageReading(readingsFromLastWeek);
        BigDecimal energyConsumed = averageReading.multiply(BigDecimal.valueOf(60))
                                    .multiply(BigDecimal.valueOf(24))
                                    .multiply(BigDecimal.valueOf(7));
        return energyConsumed.multiply(unitRate);
    }
}
