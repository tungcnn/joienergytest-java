package uk.tw.energy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;
import uk.tw.energy.service.UsageCostService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UsageCostControllerTest {
    private static final String SMART_METER_ID = "10101010";
    private static final String SMART_METER_ID2 = "10101011";
    private static final String PRICE_PLAN_1_ID = "test-supplier";
    private UsageCostController usageCostController;
    private UsageCostService usageCostService;

    private MeterReadingService meterReadingService;

    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        PricePlan pricePlan1 = new PricePlan(PRICE_PLAN_1_ID, null, BigDecimal.TEN, null);

        List<PricePlan> pricePlans = new ArrayList<>();
        pricePlans.add(pricePlan1);
        Map<String, String> meterToTariffs = new HashMap<>();
        meterToTariffs.put(SMART_METER_ID, PRICE_PLAN_1_ID);

        ElectricityReading electricityReading1 = new ElectricityReading(Instant.ofEpochMilli(1623308848), BigDecimal.valueOf(0.9000));
        ElectricityReading electricityReading2 = new ElectricityReading(Instant.ofEpochMilli(1622790448), BigDecimal.valueOf(0.1000));
        ElectricityReading electricityReading3 = new ElectricityReading(Instant.ofEpochMilli(12389797), BigDecimal.valueOf(0.9000));

        List<ElectricityReading> electricityReadings = new ArrayList<>();
        electricityReadings.add(electricityReading1);
        electricityReadings.add(electricityReading2);
        electricityReadings.add(electricityReading3);

        Map<String, List<ElectricityReading>> meterReadings = new HashMap<>();
        meterReadings.put(SMART_METER_ID, electricityReadings);
        meterReadings.put(SMART_METER_ID2, null);

        this.meterReadingService = new MeterReadingService(meterReadings);
        this.usageCostService = new UsageCostService(pricePlans);
        this.accountService = new AccountService(meterToTariffs);
        this.usageCostController = new UsageCostController(usageCostService, meterReadingService, accountService);
    }

    @Test
    public void givenNoPricePlanIdIsAttachedToSmartMeterIdWhenFetchShouldReturnErrorResponse() {
        assertThat(usageCostController.getUsageCostOfLastWeek(SMART_METER_ID2).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenSmartMeterIdWithPricePlanIdAttachedShouldReturnUsageCost() {
        Map<String, Object> expected = new HashMap<>();
        expected.put(SMART_METER_ID, 43200);
        assertThat(usageCostController.getUsageCostOfLastWeek(SMART_METER_ID).getBody().equals(expected));
    }
}
