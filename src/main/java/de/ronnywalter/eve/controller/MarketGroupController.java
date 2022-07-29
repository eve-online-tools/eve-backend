package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.*;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.MarketDataService;
import de.ronnywalter.eve.service.TypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MarketGroupController extends AbstractController {

    private final TypeService typeService;
    private final MarketDataService marketDataService;

    @GetMapping("/types")
    public List<TypeDTO> getTypes() {
        List<Type> types = typeService.getTypes();
        return mapList(types, TypeDTO.class);
    }

    @GetMapping("types/{id}")
    public TypeDTO getType (@PathVariable int id) {
        Type mg = typeService.getType(id);
        return map(mg, TypeDTO.class);
    }

    @GetMapping("groups")
    public List<MarketGroupDTO> getMarketGroups () {
        List<RegionDTO> regionDTOS = new ArrayList<>();
        List<MarketGroup> marketGroups = typeService.getRootMarketGroups(true);
        return mapList(marketGroups, MarketGroupDTO.class);
    }

    @GetMapping("groups/{id}")
    public MarketGroupDTO getMarketGroup (@PathVariable int id) {
        MarketGroup mg = typeService.getMarketGroup(id, true);
        return map(mg, MarketGroupDTO.class);
    }

    @GetMapping("groups/{id}/types")
    public List<TypeDTO> getTypesOfMarketGroup (@PathVariable int id) {
        List<Type> types = typeService.getTypesOfMarketGroup(id);
        return mapList(types, TypeDTO.class);
    }

    @GetMapping("marketdata/{regionId}/{typeId}")
    public MarketDataDTO getMarketData (@PathVariable int regionId, @PathVariable int typeId) {
        MarketData marketData = marketDataService.getMarketData(typeId, regionId);
        MarketDataDTO dto = new MarketDataDTO();

        return map(marketData, MarketDataDTO.class);
    }
}
