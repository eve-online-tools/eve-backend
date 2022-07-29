package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.MarketOrder;
import de.ronnywalter.eve.model.stats.PriceStats;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MarketOrderRepository extends CrudRepository<MarketOrder, Long> {
    //List<MarketOrder> findByRegionId(int regionId);

    List<MarketOrder> findByLocationId(long locationId);

    List<MarketOrder> findByLocationIdAndIsBuyOrder(long locationId, boolean b);

    List<MarketOrder> findByRegionId(int regionId);

    MarketOrder findFirstByLocationIdAndTypeIdAndIsBuyOrderOrderByPriceAsc(long locationId, int typeId, boolean isBuyOrder);

    MarketOrder findFirstByLocationIdAndTypeIdAndIsBuyOrderOrderByPriceDesc(long locationId, int typeId, boolean isBuyOrder);

    List<MarketOrder> findByRegionIdAndTypeId(int regionId, int typeId);

    List<MarketOrder> findByRegionIdAndTypeIdAndLocationId(Integer regionId, Integer typeId, Long locationId);


    @Query(value="SELECT " +
            "    new de.ronnywalter.eve.model.stats.PriceStats(mo.typeId, max(price)) " +
            "FROM " +
            "    MarketOrder mo " +
            "WHERE " +
            "    mo.isBuyOrder = true " +
            "AND mo.regionId = ?2 " +
            "AND mo.typeId IN (?1) " +
            "GROUP BY " +
            "    mo.typeId")
    List<PriceStats> queryBuyPrices(List<Integer> types, int regionId);

    @Query(value="SELECT " +
            "    new de.ronnywalter.eve.model.stats.PriceStats(mo.typeId, min(price)) " +
            "FROM " +
            "    MarketOrder mo " +
            "WHERE " +
            "    mo.isBuyOrder = false " +
            "AND mo.regionId = ?2 " +
            "AND mo.typeId IN (?1) " +
            "GROUP BY " +
            "    mo.typeId")
    List<PriceStats> querySellPrices(List<Integer> types, int regionId);


    @Query(value="SELECT " +
            "    new de.ronnywalter.eve.model.stats.PriceStats(mo.typeId, min(price)) " +
            "FROM " +
            "    MarketOrder mo " +
            "WHERE " +
            "    mo.isBuyOrder = false " +
            "AND mo.regionId = ?2 " +
            "AND mo.typeId IN (?1) " +
            "AND mo.locationId = ?3 " +
            "GROUP BY " +
            "    mo.typeId")
    List<PriceStats> querySellPricesForRegionAndLocation(List<Integer> types, int regionId, Long locationId);



}
