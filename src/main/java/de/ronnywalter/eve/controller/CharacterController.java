package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.CreateEveCharacterDTO;
import de.ronnywalter.eve.dto.EveCharacterDTO;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.esi.CharacterUpdateJob;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.JobSchedulerService;
import de.ronnywalter.eve.service.UniverseService;
import de.ronnywalter.eve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("chars")
@RequiredArgsConstructor
public class CharacterController extends AbstractController {

    private final CharacterService characterService;
    private final UniverseService universeService;
    private final UserService userService;
    private final JobSchedulerService jobSchedulerService;

    private final CharacterUpdateJob characterUpdateJob;

    @GetMapping(value = "/{userId}/{id}")
    @ResponseBody
    public EveCharacterDTO getCharacter(@PathVariable int userId, @PathVariable int id) {
        try{
            EveCharacter character = characterService.getEveCharacterForUser(userId, id);
            if(character != null) {
                return createDto(character);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } catch (EveCharacterNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping(value = "/{userId}/{id}")
    @ResponseBody
    public void deleteCharacter(@PathVariable int userId, @PathVariable int id) {
        if(characterService.getEveCharacterIdsForUser(userId).contains(id)) {
            characterService.deleteCharacter(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/{userId}")
    @ResponseBody
    public EveCharacterDTO createCharacter(@PathVariable int userId, @RequestBody CreateEveCharacterDTO createEveCharacterDTO) {
        EveCharacter eveCharacter = characterService.getEveCharacter(createEveCharacterDTO.getId());
        if(eveCharacter == null) {
            eveCharacter = new EveCharacter();
            eveCharacter.setId(createEveCharacterDTO.getId());
        }
        eveCharacter.setApiToken(createEveCharacterDTO.getApiToken());
        eveCharacter.setRefreshToken(createEveCharacterDTO.getRefreshToken());
        eveCharacter.setExpiryDate(createEveCharacterDTO.getExpiryDate());
        eveCharacter.setUser(userService.getUser(userId));
        characterService.saveCharacter(eveCharacter);
        characterUpdateJob.updateChar(eveCharacter.getId());
        jobSchedulerService.scheduleAllJobs();
        return getCharacter(eveCharacter.getUser().getId(), eveCharacter.getId());
    }


    @GetMapping(value = "/{userId}")
    @ResponseBody
    public List<EveCharacterDTO> getCharacters(@PathVariable int userId) {
        List<EveCharacterDTO> result = new ArrayList<>();
        return mapList(characterService.getEveCharactersForUser(userId), EveCharacterDTO.class);
    }

    private EveCharacterDTO createDto(EveCharacter character) {
        EveCharacterDTO dto = map(character, EveCharacterDTO.class);
        dto.setLocationName(character.getLocationId() != null ? universeService.getLocation(character.getLocationId()).getName() : null);
        dto.setSolarSystemName(character.getSolarSystemId() != null ? universeService.getSolarSystem(character.getSolarSystemId()).getName() : null);
        return dto;
    }

}
