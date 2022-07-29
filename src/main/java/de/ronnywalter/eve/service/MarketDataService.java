package de.ronnywalter.eve.service;

import de.ronnywalter.eve.model.MarketData;
import de.ronnywalter.eve.model.MarketHistory;
import de.ronnywalter.eve.model.MarketOrder;
import de.ronnywalter.eve.model.Type;
import de.ronnywalter.eve.model.stats.HistPriceStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final TypeService typeService;
    private final MarketOrderService marketOrderService;
    private final MarketHistoryService marketHistoryService;

    public MarketData getMarketData(Integer typeId, Integer regionId) {
        return getMarketData(typeId, regionId, null);
    }

    public MarketData getMarketData(Integer typeId, Integer regionId, Long locationId) {
        Type type = typeService.getType(typeId);

        List<MarketOrder> marketOrderList;
        if(locationId == null) {
            marketOrderList = marketOrderService.getMarketOrdersForRegionAndTypeId(regionId, typeId);
        } else {
            marketOrderList = marketOrderService.getMarketOrdersForRegionAndTypeIdAndLocationId(regionId, typeId, locationId);
        }

        List<MarketOrder> buyOrders = marketOrderList.stream().filter(m -> m.isBuyOrder()).sorted(Comparator.comparing(MarketOrder::getPrice).reversed()).collect(Collectors.toList());
        List<MarketOrder> sellOrders = marketOrderList.stream().filter(m -> !m.isBuyOrder()).sorted(Comparator.comparing(MarketOrder::getPrice)).collect(Collectors.toList());

        MarketData md = new MarketData();
        md.setType(type);
        md.setBuyOrders(buyOrders);
        md.setSellOrders(sellOrders);

        // historical data
        HistPriceStats histPriceStats5d = marketHistoryService.getStats(regionId, typeId, 5);
        if(histPriceStats5d != null) {
            md.setAveragePrice5d(histPriceStats5d.getAvgPrice());
            md.setAverageVolume5d(histPriceStats5d.getAvgVolume());
        }

        HistPriceStats histPriceStats20d = marketHistoryService.getStats(regionId, typeId, 20);
        if(histPriceStats20d != null) {
            md.setAveragePrice20d(histPriceStats20d.getAvgPrice());
            md.setAverageVolume20d(histPriceStats20d.getAvgVolume());
        }

        return md;
    }







}
