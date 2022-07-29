package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.MyOrderDTO;
import de.ronnywalter.eve.dto.TransactionDTO;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.MyOrder;
import de.ronnywalter.eve.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("myorders")
@RequiredArgsConstructor
@Slf4j
public class MyOrderController extends AbstractController {

    private final MyOrderService myOrderService;
    private final UniverseService universeService;
    private final CharacterService characterService;
    private final CorporationService corporationService;


    @GetMapping(value = "/{userId}/buy")
    @ResponseBody
    public List<MyOrderDTO> getBuyOrders(@PathVariable int userId) {
        List<MyOrder> orders = myOrderService.getBuyOrdersForUser(userId);
        List<MyOrderDTO> orderDTOS = toDtos(orders);

        return orderDTOS;
    }

    private List<MyOrderDTO> toDtos(List<MyOrder> orders) {
        List<MyOrderDTO> orderDTOS = mapList(orders, MyOrderDTO.class);
        orderDTOS.forEach(myOrderDTO -> {
            myOrderDTO.setLocation(universeService.getLocation(myOrderDTO.getLocationId()).getName());
            myOrderDTO.setCharacterName(characterService.getEveCharacter(myOrderDTO.getCharacterId()).getName());
            if(myOrderDTO.isCorpOrder()) {
                Corporation corporation = corporationService.getCorporation(myOrderDTO.getCorpId());
                myOrderDTO.setCorpName(corporation.getName());
                myOrderDTO.setCorpTicker(corporation.getTicker());
            }
        });
        return orderDTOS;
    }

    @GetMapping(value = "/{userId}/sell")
    @ResponseBody
    public List<MyOrderDTO> getSellOrders(@PathVariable int userId) {
        List<MyOrder> orders = myOrderService.getSellOrdersForUser(userId);
        List<MyOrderDTO> orderDTOS = toDtos(orders);

        return orderDTOS;
    }

}
