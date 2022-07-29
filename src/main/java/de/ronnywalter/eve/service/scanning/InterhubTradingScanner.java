package de.ronnywalter.eve.service.scanning;


import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.Mark;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.model.stats.PriceStats;
import de.ronnywalter.eve.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterhubTradingScanner {

    private final TypeService typeService;
    private final MarketDataService marketDataService;
    private final MarketOrderService marketOrderService;
    private final MarketHistoryService marketHistoryService;
    private final UniverseService universeService;
    private final TradingConfigService tradingConfigService;

    public List<TradeCandidate> findTradeCanditates() {
        RegionTradingConfig config = tradingConfigService.getConfig(432);

        double margin = config.getMinMargin();
        List<TradeCandidate> candidates = new ArrayList<>();
        List<Type> types = typeService.getTypesOfMarketGroup(9);

        Region from = universeService.getRegion(config.getBuyRegionId());
        Region to = universeService.getRegion(config.getSellRegionId());

        long start = System.currentTimeMillis();

        Map<Integer, Double> pricesFrom = marketOrderService.getSellPricesForRegionAndLocation(types.stream().map(Type::getId).collect(Collectors.toList()), from.getId(), config.getBuyLocationId());
        Map<Integer, Double> pricesTo = marketOrderService.getSellPricesForRegionAndLocation(types.stream().map(Type::getId).collect(Collectors.toList()), to.getId(), config.getSellLocationId());


        types.forEach(t-> {
            log.info("Processing type: " + t.getName());

            if(pricesFrom.containsKey(t.getId()) && pricesTo.containsKey(t.getId())) {
                Double fromPrice = pricesFrom.get(t.getId());
                Double toPrice = pricesTo.get(t.getId());

                if(toPrice >= fromPrice * (1+margin)) {
                    log.info("Interesting: " + t.getName());
                    TradeCandidate tc = getTradeCandidate(t, config);

                    if(tc != null) {
                        candidates.add(tc);
                    }
                }
            } else {
                log.info("Skipping Type: " + t.getName() + ", because of missing prices.");
            }

        });
        System.out.println("Time: " + ((System.currentTimeMillis() - start)/1000));

        return candidates;
    }


    private TradeCandidate getTradeCandidate(Type t, RegionTradingConfig config) {
        MarketData marketDataFrom = marketDataService.getMarketData(t.getId(), config.getBuyRegionId(), config.getBuyLocationId());
        MarketData marketDataTo = marketDataService.getMarketData(t.getId(), config.getSellRegionId(), config.getSellLocationId());

        // sellPrice = 1 + margin
        // buyPrice = 1
        // maxBuyPrice = SellPrice in Target region / 1 + margin

        double sellPrice = marketDataTo.getSellPrice();
        double buyPrice = marketDataFrom.getSellPrice();

        double maxBuyPrice = sellPrice / (1 + config.getMinMargin());

        double volumeInToRegion = marketDataTo.getAverageVolume5d();
        int possibleVolume = (Long.valueOf(Math.round(Math.min(volumeInToRegion * 5, marketDataFrom.getVolumeForPrice(maxBuyPrice)))).intValue());

        int numberOfModifiedOrders = marketDataTo.getNumberOfSellOrderUpdates(config.getDaysModifiedOrders());
        double avgBuyPrice = marketDataFrom.getAverageSellPriceForVolume(possibleVolume);
        double avgSellPrice = marketDataTo.getAverageSellPriceForVolume(possibleVolume);

        double averageProfitPerItem = avgSellPrice - avgBuyPrice;
        double averageProfit = possibleVolume * (avgSellPrice - avgBuyPrice);
        double averageProfitPct = (avgSellPrice / avgBuyPrice) - 1;
        double averageProfitAtCurrentSellPrices = possibleVolume * (sellPrice - avgBuyPrice);
        double averageProfitPctAtCurrentSellPrices = (sellPrice / avgBuyPrice) - 1;

        double cargo = possibleVolume * t.getPackagedVolume();


        if (numberOfModifiedOrders <= config.getMaxModifiedOrdersWithinDays() && possibleVolume > 0) {

            TradeCandidate tc = new TradeCandidate(config.getId(), t.getId());
            tc.setBuyRegionId(config.getBuyRegionId());
            tc.setSellRegionId(config.getSellRegionId());
            tc.setCurrentBuyPrice(buyPrice);
            tc.setCurrentSellPrice(sellPrice);
            tc.setCurrentProfitPerItem(sellPrice - buyPrice);
            tc.setCurrentProfitPct((sellPrice / buyPrice) - 1);
            tc.setMaxBuyPrice(maxBuyPrice);
            tc.setPossibleVolume(possibleVolume);
            tc.setAverageBuyPrice(avgBuyPrice);
            tc.setAverageSellPrice(avgSellPrice);
            tc.setAverageProfitPerItem(averageProfitPerItem);
            tc.setAverageProfit(averageProfit);
            tc.setAverageProfitPct(averageProfitPct);

            tc.setAverageProfitAtCurrentSellPrices(averageProfitAtCurrentSellPrices);
            tc.setAverageProfitPctAtCurrentSellPrices(averageProfitPctAtCurrentSellPrices);

            tc.setModifiedOrdersInSellRegion(numberOfModifiedOrders);
            tc.setCargo(cargo);

            tc.setAverageVolumeSellRegion(volumeInToRegion);

            return tc;
        }


        return null;
    }
}
