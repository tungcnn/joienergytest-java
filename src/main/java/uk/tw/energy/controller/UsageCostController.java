package uk.tw.energy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.UsageCostService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usages")
public class UsageCostController {
    private final UsageCostService usageCostService;

    private final MeterReadingService meterReadingService;

    private final AccountService accountService;

    public UsageCostController(UsageCostService usageCostService,
                               MeterReadingService meterReadingService,
                               AccountService accountService) {
        this.usageCostService = usageCostService;
        this.meterReadingService = meterReadingService;
        this.accountService = accountService;
    }

    @GetMapping("/week/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> getUsageCostOfLastWeek(@PathVariable("smartMeterId") String smartMeterId) {
        Optional<String> pricePlanId = Optional.ofNullable(accountService.getPricePlanIdForSmartMeterId(smartMeterId));

        if (!pricePlanId.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        if (!readings.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        BigDecimal usageCost = usageCostService.calculateUsageCostOfLastWeek(pricePlanId.get(), readings.get());
        Map<String, Object> result = new HashMap<>();
        result.put(smartMeterId, usageCost);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
