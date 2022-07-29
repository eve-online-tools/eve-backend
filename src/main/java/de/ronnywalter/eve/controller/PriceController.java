package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
@Slf4j
public class PriceController {

    private final MarketOrderService marketOrderService;

    @GetMapping("/sellprice/{locationId}/{typeId}")
    public Double getSellPrice(@PathVariable long locationId, @PathVariable int typeId) {
        return marketOrderService.getPrice(typeId, locationId);
    }

    @GetMapping("/buyprices/{locationId}/{typeId}")
    public Double getBuyPrice(@PathVariable long locationId, @PathVariable int typeId) {
        return marketOrderService.getBuyPrice(typeId, locationId);
    }
}
