package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.ConstellationDTO;
import de.ronnywalter.eve.dto.RegionDTO;
import de.ronnywalter.eve.model.Constellation;
import de.ronnywalter.eve.model.Region;
import de.ronnywalter.eve.service.UniverseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("universe")
@RequiredArgsConstructor
@Slf4j
public class UniverseController extends AbstractController {

    private final UniverseService universeService;

    @GetMapping("/regions")
    public List<RegionDTO> getRegions () {
        List<RegionDTO> regionDTOS = new ArrayList<>();
        return mapList(universeService.getRegions(), RegionDTO.class);
    }

    @GetMapping("/regions/{regionId}")
    public RegionDTO getRegion (@PathVariable Integer regionId) {
        Region savedRegion = universeService.getRegion(regionId);
        return map(savedRegion, RegionDTO.class);
    }

    @GetMapping("/constellations")
    public List<ConstellationDTO> getConstellations () {
        List<RegionDTO> regionDTOS = new ArrayList<>();
        return mapList(universeService.getConstellations(), ConstellationDTO.class);
    }

    @GetMapping("/constellations/{constellationId}")
    public ConstellationDTO getConstellation (@PathVariable Integer constellationId) {
        Constellation constellation = universeService.getConstellation(constellationId);
        return map(constellation, ConstellationDTO.class);
    }
}
