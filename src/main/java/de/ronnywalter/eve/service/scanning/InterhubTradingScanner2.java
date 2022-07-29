package de.ronnywalter.eve.service.scanning;


import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.MarketHistoryService;
import de.ronnywalter.eve.service.MarketOrderService;
import de.ronnywalter.eve.service.TypeService;
import de.ronnywalter.eve.service.UniverseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterhubTradingScanner2 {

    private final TypeService typeService;
    private final MarketOrderService marketOrderService;
    private final MarketHistoryService marketHistoryService;
    private final UniverseService universeService;

    /*
    public List<TradeCandidate> findTradeCanditates(InterhubScanningConfig config, String csvFile) {
        List<TradeCandidate> candidates = findTradeCanditates(config);

        if(csvFile != null) {
            try {
                FileWriter out = new FileWriter(csvFile);

                String[] header = new String[] {"Name","category","volumeInSellRegion", "quantity","avgBuy","avgSellRegion", "profit-avgbuy/avgsell", "profit-avg-sell", "cost","Sell", "profitPct-current-sell","profit-current-sell","size","cargo", "modified Orders today"};
                CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.EXCEL.withHeader(header).withDelimiter(';'));

                NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

                for (TradeCandidate c: candidates) {
                    Type type = typeService.getType(c.getTypeId());
                    String typeName = type.getName();
                    int volumeInSellRegion = c.getAverageVolumeSellRegion(7);
                    int volumeWithinMargin = c.getVolumeWithinMargin(config.getMinMargin());
                    int quantity = Math.min(volumeWithinMargin, volumeInSellRegion);
                    double averageBuyPrice = c.getAverageBuyPriceForVolume(quantity);
                    double averagePriceInSellRegion = c.getAveragePriceSellRegion(7);
                    double cost = averageBuyPrice * quantity;
                    double sellPrice = c.getBestOrderSellRegion();
                    double profitForCurrentSellPricePct = (sellPrice - averageBuyPrice) / averageBuyPrice;
                    double profitForAverageSellPricePct = (averagePriceInSellRegion - averageBuyPrice) / averageBuyPrice;
                    double profit = (sellPrice * quantity) - (averageBuyPrice * quantity);
                    double profitAvgSellPrice = (averagePriceInSellRegion * quantity) - (averageBuyPrice * quantity);

                    double cargo = type.getPackagedVolume() * quantity;

                    int numberOfModifiedToday = c.getNumberOfOrderUpdatesInSellRegionWithinDays(1);

                    csvPrinter.printRecord(typeName, type.getCategoryId(), volumeInSellRegion, quantity, nf.format(averageBuyPrice),nf.format(averagePriceInSellRegion), nf.format(profitForAverageSellPricePct), nf.format(profitAvgSellPrice), nf.format(cost), nf.format(sellPrice),  nf.format(profitForCurrentSellPricePct), nf.format(profit), nf.format(type.getPackagedVolume()), nf.format(cargo), numberOfModifiedToday);
                }
                csvPrinter.close();
                out.close();
                log.info("Results written to " + csvFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return candidates;
    }

    public List<TradeCandidate> findTradeCanditates(InterhubScanningConfig config) {

        Location from = universeService.getLocation(config.getFromLocationId());
        Location to = universeService.getLocation(config.getToLocationId());

        log.info("Getting sellorders from location " + from.getName());
        Multimap<Integer, MarketOrder> fromLocationSellOrders = marketOrderService.getSellMarketOrdersForLocationAsMap(from.getId());
        log.info("Getting sellorders from location " + to.getName());
        Multimap<Integer, MarketOrder> toLocationSellOrders = marketOrderService.getSellMarketOrdersForLocationAsMap(to.getId());
        //log.info("Getting buyorders from location " + to.getName());
        //Map<Type, List<MarketOrder>> toLocationSellOrders = marketOrderService.getCurrentBuyOrdersForLocationAndType(to, types);

        List<TradeCandidate> candidates = new ArrayList<>();
        List<Integer> myTypes = new ArrayList<>();
        myTypes.add(32780);

        for (Integer typeId : fromLocationSellOrders.keySet()) {
        //for (Integer typeId : myTypes) {
            log.debug("Processing type: " + typeId);
            List<MarketOrder> sellOrdersFrom = Lists.newArrayList(fromLocationSellOrders.get(typeId));
            List<MarketOrder> sellOrdersTo = Lists.newArrayList(toLocationSellOrders.get(typeId));

            if(sellOrdersFrom != null && sellOrdersFrom.size() > 0 && sellOrdersTo != null && sellOrdersTo.size() > 0) {
                Collections.sort(sellOrdersFrom, new MarketOrderPriceAscComparator());
                Collections.sort(sellOrdersTo, new MarketOrderPriceAscComparator());
                //Collections.sort(sellOrdersTo, new MarketOrderPriceDescComparator());

                double bestSellFrom = sellOrdersFrom.get(0).getPrice();
                double bestSellTo = sellOrdersTo.get(0).getPrice();

                double diff = bestSellTo - bestSellFrom;
                double pct = diff / bestSellFrom;

                //log.debug("type: " + typeId + ",buy@" + bestSellFrom + ", sell@" + bestSellTo + ", profit: " + pct);

                if(pct > config.getMinMargin()) {
                    Type type = typeService.getType(typeId);
                    log.info("Interesting item: " + type.getName() + ", buy@" + bestSellFrom + ", sell@" + bestSellTo + ", pct: " + ((bestSellTo / bestSellFrom * 100) - 100));


                    // TODO: implement!
                    List<MarketHistory> historyFromRegion = marketHistoryService.getMarketHistoryForTypeAndRegion(0, typeId);
                    List<MarketHistory> historyToRegion = marketHistoryService.getMarketHistoryForTypeAndRegion(0, typeId);

                    TradeCandidate c = TradeCandidate.builder().typeId(typeId).ordersBuy(sellOrdersFrom).ordersSell(sellOrdersTo).historyFromRegion(historyFromRegion).historyToRegion(historyToRegion).build();
                    candidates.add(c);
                }
            }
        }
        log.info("got " + candidates.size() + " candidates. Now filtering....");

        Iterator<TradeCandidate> it = candidates.iterator();
        while(it.hasNext()) {
            TradeCandidate c = it.next();

            long averageVolumeBuyRegion = c.getAverageVolumeBuyRegion(config.getDaysVolume());
            long averageVolumeSellRegion = c.getAverageVolumeSellRegion(config.getDaysVolume());
            if(averageVolumeBuyRegion < config.getMinVolumeBuyRegion() || averageVolumeSellRegion < config.getMinVolumeSellRegion()) {
                log.info(c.getTypeId() + " has to less volume. buy-region: " + averageVolumeBuyRegion + ", sell-region: " + averageVolumeSellRegion + ". Skipping it.");
                it.remove();
            }
        }

        it = candidates.iterator();
        while(it.hasNext()) {
            TradeCandidate c = it.next();

            int modifyCount = c.getNumberOfOrderUpdatesInSellRegionWithinDays(config.getDaysModifiedOrders());
            if(modifyCount > config.getMaxModifiedOrdersWithinDays()) {
                log.info(c.getTypeId() + " has " + modifyCount + " updated orders within last " + config.getDaysModifiedOrders() + " days. Skipping it.");
                it.remove();
            }
        }


        for (TradeCandidate candiate : candidates) {
            int volumeWithinMargin = candiate.getVolumeWithinMargin(config.getMinMargin());
            int volumeSellRegion = candiate.getAverageVolumeSellRegion(7);
            double avgBuyPrice = candiate.getAverageBuyPriceForVolume(volumeSellRegion);

            log.info(candiate.getTypeId() + ": avg(volume, 7) in buy-Region: " + candiate.getAverageVolumeBuyRegion(7) + ", avg(volume, 7) in sell-Region: " + candiate.getAverageVolumeSellRegion(7) + " possible Profit per Item: " + candiate.getPossibleProfitPerItem() + ", buy@" + candiate.getBestOrderBuyRegion() + " (" + candiate.getVolumeBestOrderBuyRegion() + "), sell@" + candiate.getBestOrderSellRegion() + "(" + candiate.getVolumeWithinMargin(config.getMinMargin()) + "), avg-buy-price: " + avgBuyPrice);
        }
        log.info("Returning " + candidates.size() + " candidates.");

        return candidates;
    }

     */
}
