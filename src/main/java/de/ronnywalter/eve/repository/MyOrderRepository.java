package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.MarketHistoryMaxDate;
import de.ronnywalter.eve.model.MyOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface MyOrderRepository extends CrudRepository<MyOrder, Long> {
    List<MyOrder> findByCharacterIdInOrCorpIdInOrderByType(List<Integer> eveCharacterIdsForUser, List<Integer> corporationIdsForUser);

    List<MyOrder> findByIsBuyOrderAndCharacterIdInOrCorpIdInOrderByType(boolean isBuyOrder, List<Integer> eveCharacterIdsForUser, List<Integer> corporationIdsForUser);

    @Query("SELECT mo FROM #{#entityName} mo where characterId in ?1 and isBuyOrder = ?2 and state='open'")
    List<MyOrder> findOpenOrdersForCharsAndIsBuyOrder(List<Integer> charIds, boolean isBuy);

}
