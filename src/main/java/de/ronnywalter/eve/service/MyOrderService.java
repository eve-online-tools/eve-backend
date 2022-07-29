package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.MyOrder;
import de.ronnywalter.eve.repository.MyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyOrderService {

    private final MyOrderRepository myOrderRepository;
    private final CharacterService characterService;
    private final CorporationService corporationService;

    public MyOrder saveOrder(MyOrder myOrder) {
        return myOrderRepository.save(myOrder);
    }

    public List<MyOrder> saveOrders(List<MyOrder> myOrders) {
        return Lists.newArrayList(myOrderRepository.saveAll(myOrders));
    }

    public List<MyOrder> getSellOrdersForUser(int userId) {
        return myOrderRepository.findOpenOrdersForCharsAndIsBuyOrder(
                characterService.getEveCharacterIdsForUser(userId),
                false);
    }

    public List<MyOrder> getBuyOrdersForUser(int userId) {
        return myOrderRepository.findOpenOrdersForCharsAndIsBuyOrder(
                characterService.getEveCharacterIdsForUser(userId),
                true);
    }
}
