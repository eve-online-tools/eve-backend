package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.RegionTradingConfigDTO;
import de.ronnywalter.eve.model.RegionTradingConfig;
import de.ronnywalter.eve.service.TradingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("trading/region")
@RequiredArgsConstructor
@Slf4j
public class InterhubTradingController extends AbstractController {

    private final TradingConfigService interhubScanningService;

    @PostMapping("/")
    public RegionTradingConfigDTO newConfig (@RequestBody RegionTradingConfigDTO dto) {

        RegionTradingConfig rtc = map(dto, RegionTradingConfig.class);
        interhubScanningService.saveConfig(rtc);
        return dto;
    }

}
