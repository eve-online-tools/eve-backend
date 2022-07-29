package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.RegionTradingConfig;
import de.ronnywalter.eve.repository.RegionTradingConfigRepository;
import de.ronnywalter.eve.service.scanning.InterhubTradingScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradingConfigService {

    private final RegionTradingConfigRepository regionTradingConfigRepository;

    public RegionTradingConfig saveConfig(RegionTradingConfig config) {
        return regionTradingConfigRepository.save(config);
    }

    public RegionTradingConfig getConfig(Integer id) {
        return regionTradingConfigRepository.findById(id).orElse(null);
    }

    public List<RegionTradingConfig> getConfigs() {
        return Lists.newArrayList(regionTradingConfigRepository.findAll());
    }
}
