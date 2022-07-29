package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.MarketHistory;
import de.ronnywalter.eve.model.MarketHistoryMaxDate;
import de.ronnywalter.eve.model.stats.HistPriceStats;
import de.ronnywalter.eve.repository.MarketHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketHistoryService {

    private final MarketHistoryRepository marketHistoryRepository;

    public MarketHistory saveMarketHistory(MarketHistory marketHistory) {
        return marketHistoryRepository.save(marketHistory);
    }

    public List<MarketHistory> saveAllMarketHistory(List<MarketHistory> marketHistory) {
        return Lists.newArrayList(marketHistoryRepository.saveAll(marketHistory));
    }

    public List<MarketHistory> saveNewMarketHistoryForRegion(Integer regionId, Integer typeId, List<MarketHistory> marketHistory) {
        LocalDate maxDateForRegion = marketHistoryRepository.findMaxDateForRegionAndType(regionId, typeId);
        List<MarketHistory> toSave = new ArrayList<>();
        if(maxDateForRegion != null) {
            marketHistory.forEach(mh -> {
                if (mh.getDate().isAfter(maxDateForRegion)) {
                    toSave.add(mh);
                }
            });
        } else {
            toSave.addAll(marketHistory);
        }
        marketHistoryRepository.saveAll(toSave);
        return toSave;
    }

    public List<MarketHistory> getMarketHistoryForTypeAndRegion(int regionId, int typeId) {
        return marketHistoryRepository.findTop30ByIdRegionIdAndIdTypeIdOrderByIdDateDesc(regionId, typeId);
    }

    public List<MarketHistoryMaxDate> getMaxDates(int regionId) {
        return marketHistoryRepository.findMaxDatesForRegionAndType(regionId);
    }

    public Map<Integer, LocalDate> getMaxDatesAsMap(int regionId) {
        Map<Integer, LocalDate> result = new HashMap<>();
        getMaxDates(regionId).forEach(t -> result.put(t.getTypeId(), t.getDate()));
        return result;
    }

    public HistPriceStats getStats(int region, int typeId, int days) {
        LocalDate from = LocalDate.now().minusDays(days + 1);
        return marketHistoryRepository.getHistStats(typeId, region, from);
    }
}
