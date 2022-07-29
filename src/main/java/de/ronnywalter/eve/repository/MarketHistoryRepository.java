package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.MarketHistory;
import de.ronnywalter.eve.model.MarketHistoryMaxDate;
import de.ronnywalter.eve.model.stats.HistPriceStats;
import de.ronnywalter.eve.model.stats.PriceStats;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;


public interface MarketHistoryRepository extends CrudRepository<MarketHistory, Long> {
    List<MarketHistory> findTop30ByIdRegionIdAndIdTypeIdOrderByIdDateDesc(int regionId, int typeId);

    //@Query (value = "SELECT max(id.date) FROM #{#entityName} mh where mh.id.region.id=?1 and mh.id.type.id=?2")
    @Query (value = "SELECT date FROM market_history mh where mh.region_id=?1 and mh.type_id=?2 order by date desc LIMIT 1" , nativeQuery = true)
    LocalDate findMaxDateForRegionAndType(Integer regionId, Integer typeId);

    @Query("SELECT new de.ronnywalter.eve.model.MarketHistoryMaxDate(mh.id.typeId, MAX(mh.id.date)) "
            + "FROM #{#entityName} mh where mh.id.regionId=?1 GROUP BY mh.id.typeId")
    List<MarketHistoryMaxDate> findMaxDatesForRegionAndType(int regionId);

    @Query(value="SELECT " +
            "    new de.ronnywalter.eve.model.stats.HistPriceStats(mh.id.typeId, avg(mh.average), avg(mh.volume)) " +
            "FROM " +
            "    #{#entityName} mh " +
            "WHERE " +
            "    mh.id.regionId = ?2 " +
            "AND mh.id.typeId = ?1 " +
            "AND mh.id.date >= ?3 " +
            "GROUP BY " +
            "    mh.id.typeId")
    HistPriceStats getHistStats(Integer typeId, int regionId, LocalDate fromDate);

}
