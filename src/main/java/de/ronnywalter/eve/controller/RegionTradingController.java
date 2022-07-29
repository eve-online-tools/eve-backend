package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.CorpDTO;
import de.ronnywalter.eve.dto.TradeCandidateDTO;
import de.ronnywalter.eve.dto.TypeDTO;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.model.TradeCandidate;
import de.ronnywalter.eve.service.CorporationService;
import de.ronnywalter.eve.service.TypeService;
import de.ronnywalter.eve.service.scanning.InterhubTradingScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.TypeSafeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("regiontrading")
@RequiredArgsConstructor
@Slf4j
public class RegionTradingController extends AbstractController {

    private final InterhubTradingScanner interhubTradingScanner;
    private final TypeService typeService;

    @GetMapping(value = "")
    @ResponseBody
    public List<TradeCandidateDTO> getTradeCandidates() {
        List<TradeCandidateDTO> dtos = new ArrayList<>();
        interhubTradingScanner.findTradeCanditates().forEach(tc -> {
            TradeCandidateDTO dto = map(tc, TradeCandidateDTO.class);
            dto.setType(map(typeService.getType(tc.getId().getTypeId()), TypeDTO.class));
            dtos.add(dto);
        });

        return dtos;
    }
}
