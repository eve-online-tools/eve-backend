package de.ronnywalter.eve.jobs.scanning;

import de.ronnywalter.eve.jobs.AbstractJob;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
//@SchedulableJob(scheduleTime = "0 0 * * * *")
public class RegionTradingScanningJob extends AbstractJob {

    @Autowired
    private TypeService typeService;

    @Autowired
    private TradingConfigService tradingConfigService;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private MarketOrderService marketOrderService;

    @Autowired
    private TradeCandidateService tradeCandidateService;

    public RegionTradingScanningJob() {
        super(RegionTradingScanningJob.class.getName());
    }


    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<RegionTradingConfig> configs = tradingConfigService.getConfigs();

        configs.forEach(config -> {
            Map<String, String> params = new HashMap<>();
            params.put("id", "" + config.getId());
            JobData jobData = initJobData(config.getName());

            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }

    @Override
    public void run(JobData jobData) {
        Integer id = Integer.parseInt(jobData.getJobParams().get("id"));
        RegionTradingConfig config = tradingConfigService.getConfig(id);
        List<TradeCandidate> candidates = findTradeCanditates(config);
        tradeCandidateService.saveTradeCandidates(candidates);
    }

    private List<TradeCandidate> findTradeCanditates(RegionTradingConfig config) {
        List<TradeCandidate> candidates = new ArrayList<>();
        List<MarketGroup> groups = typeService.getRootMarketGroups(true);
        List<Type> types = new ArrayList<>();
        groups.forEach(g -> types.addAll(typeService.getTypesOfMarketGroup(g)));

        List<Integer> typeIds = types.stream().map(Type::getId).collect(Collectors.toList());
        Map<Integer, Double> pricesBuyRegion = marketOrderService.getSellPrices(typeIds, config.getBuyRegionId());
        Map<Integer, Double> pricesSellRegion = marketOrderService.getSellPrices(typeIds, config.getSellRegionId());

        long start = System.currentTimeMillis();

        types.forEach(t-> {
            if(pricesBuyRegion.containsKey(t.getId()) && pricesSellRegion.containsKey(t.getId())) {
                Double fromPrice = pricesBuyRegion.get(t.getId());
                Double toPrice = pricesSellRegion.get(t.getId());

                if(toPrice > fromPrice * (1+config.getMinMargin())) {
                    log.info("Processing type: " + t.getName());
                    TradeCandidate tc = getTradeCandidate(t, config);

                    if (tc != null) {
                        candidates.add(tc);
                    }
                }
            }
        });
        System.out.println("Time: " + ((System.currentTimeMillis() - start)/1000));

        return candidates;
    }


    private TradeCandidate getTradeCandidate(Type t, RegionTradingConfig config) {
        MarketData marketDataFrom = marketDataService.getMarketData(t.getId(), config.getBuyRegionId());
        MarketData marketDataTo = marketDataService.getMarketData(t.getId(), config.getSellRegionId());

        // sellPrice = 1 + margin
        // buyPrice = 1
        // maxBuyPrice = SellPrice in Target region / 1 + margin

        double sellPrice = marketDataTo.getSellPrice();
        double buyPrice = marketDataFrom.getSellPrice();

        double maxBuyPrice = sellPrice / (1 + config.getMinMargin());

        double volumeInToRegion = marketDataTo.getAverageVolume5d();
        int possibleVolume = (Long.valueOf(Math.round(Math.min(volumeInToRegion * 5, marketDataFrom.getVolumeForPrice(maxBuyPrice)))).intValue());
/*
        int numberOfModifiedOrders = marketDataTo.getNumberOfSellOrderUpdates();
        double avgBuyPrice = marketDataFrom.getAverageSellPriceForVolume(possibleVolume);
        double avgSellPrice = marketDataTo.getAverageSellPriceForVolume(possibleVolume);


        double averagePriceInToRegion = marketDataTo.getAverageVolume5d();

        double averageProfitPerItem = avgSellPrice - avgBuyPrice;
        double averageProfit = possibleVolume * (avgSellPrice - avgBuyPrice);
        double averageProfitPct = (avgSellPrice / avgBuyPrice) - 1;


        if (volumeInToRegion > 50) {

            TradeCandidate tc = new TradeCandidate(config.getId(), t.getId());
            tc.setBuyRegionId(config.getBuyRegionId());
            tc.setSellRegionId(config.getSellRegionId());
            tc.setCurrentProfitPerItem(marketDataTo.getSellPrice() - marketDataFrom.getSellPrice());
            tc.setCurrentProfitPct(tc.get() * volumeInToRegion);
            tc.setMaxBuyPrice(maxBuyPrice);
            tc.setPossibleVolume(possibleVolume);
            tc.setAverageBuyPrice(avgBuyPrice);
            tc.setAverageSellPrice(avgSellPrice);
            tc.setAverageProfitPerItem(averageProfitPerItem);
            tc.setAverageProfit(averageProfit);
            tc.setAverageProfitPct(averageProfitPct);
            tc.setProfitPctCurrentSell((sellPrice / avgBuyPrice) - 1);

            return tc;
        }
*/

        return null;
    }

}
