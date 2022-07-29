package de.ronnywalter.eve.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.ronnywalter.eve.model.Location;
import de.ronnywalter.eve.model.MarketOrder;
import de.ronnywalter.eve.model.Type;
import de.ronnywalter.eve.model.stats.PriceStats;
import de.ronnywalter.eve.repository.MarketOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketOrderService {

    private final MarketOrderRepository marketOrderRepository;

    public MarketOrder saveMarketOrder(MarketOrder marketOrder) {
        return marketOrderRepository.save(marketOrder);
    }

    public List<MarketOrder> saveMarketOrders(List<MarketOrder> marketOrders) {
        return Lists.newArrayList(marketOrderRepository.saveAll(marketOrders));
    }

    public List<MarketOrder> getMarketOrdersForRegion(int regionId) {
        return marketOrderRepository.findByRegionId(regionId);
    }

    public List<MarketOrder> getMarketOrdersForRegionAndTypeId(int regionId, int typeId) {
        return marketOrderRepository.findByRegionIdAndTypeId(regionId, typeId);
    }

    public List<MarketOrder> getMarketOrdersForRegionAndTypeIdAndLocationId(Integer regionId, Integer typeId, Long locationId) {
        return marketOrderRepository.findByRegionIdAndTypeIdAndLocationId(regionId, typeId, locationId);
    }

    public List<MarketOrder> getMarketOrdersForLocation(long locationId) {
        return marketOrderRepository.findByLocationId(locationId);
    }

    public List<MarketOrder> getSellMarketOrdersForLocation(long locationId) {
        return marketOrderRepository.findByLocationIdAndIsBuyOrder(locationId, false);
    }

    public List<MarketOrder> getBuyMarketOrdersForLocation(long locationId) {
        return marketOrderRepository.findByLocationIdAndIsBuyOrder(locationId, true);
    }

    public Multimap<Integer,MarketOrder> getSellMarketOrdersForLocationAsMap(long locationId) {
        Multimap<Integer, MarketOrder> orders = ArrayListMultimap.create();
        marketOrderRepository.findByLocationIdAndIsBuyOrder(locationId, false).forEach(m -> orders.put(m.getTypeId(), m));
        return orders;
    }

    public Multimap<Integer,MarketOrder> getBuyMarketOrdersForLocationAsMap(long locationId) {
        Multimap<Integer, MarketOrder> orders = ArrayListMultimap.create();
        marketOrderRepository.findByLocationIdAndIsBuyOrder(locationId, true).forEach(m -> orders.put(m.getTypeId(), m));
        return orders;
    }

    public void deleteMarketOrders(ArrayList<MarketOrder> orders) {
        marketOrderRepository.deleteAll(orders);
    }

    public Double getPrice(Type type, Location location) {
        return this.getPrice(type.getId(), location.getId());
    }

    public Double getPrice(int typeId, long locationId) {
        MarketOrder mo = marketOrderRepository.findFirstByLocationIdAndTypeIdAndIsBuyOrderOrderByPriceAsc(locationId, typeId, false);
        if(mo != null) {
            return mo.getPrice();
        } else {
            return -1d;
        }
    }

    public Double getBuyPrice(int typeId, long locationId) {
        MarketOrder mo = marketOrderRepository.findFirstByLocationIdAndTypeIdAndIsBuyOrderOrderByPriceDesc(locationId, typeId, true);
        if(mo != null) {
            return mo.getPrice();
        } else {
            return -1d;
        }
    }

    public Map<Integer, Double> getBuyPrices(List<Integer> types, int regionId, Long locationId) {
        List<PriceStats> stats = marketOrderRepository.queryBuyPrices(types, regionId);
        Map<Integer, Double> result = new HashMap<>();
        stats.forEach(ps -> result.put(ps.getTypeId(), ps.getPrice()));
        return result;
    }

    public Map<Integer, Double> getSellPrices(List<Integer> types, int regionId) {
        List<PriceStats> stats = marketOrderRepository.querySellPrices(types, regionId);
        Map<Integer, Double> result = new HashMap<>();
        stats.forEach(ps -> result.put(ps.getTypeId(), ps.getPrice()));
        return result;
    }

    public Map<Integer, Double> getSellPricesForRegionAndLocation(List<Integer> types, int regionId, Long locationId) {
        List<PriceStats> stats = marketOrderRepository.querySellPricesForRegionAndLocation(types, regionId, locationId);
        Map<Integer, Double> result = new HashMap<>();
        stats.forEach(ps -> result.put(ps.getTypeId(), ps.getPrice()));
        return result;
    }

}
