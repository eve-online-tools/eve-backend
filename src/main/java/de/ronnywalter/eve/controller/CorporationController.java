package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.CorpDTO;
import de.ronnywalter.eve.dto.EveCharacterDTO;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.esi.CorpUpdateJob;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.service.CorporationService;
import de.ronnywalter.eve.service.JobSchedulerService;
import de.ronnywalter.eve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("corps")
@RequiredArgsConstructor
@Slf4j
public class CorporationController extends AbstractController {

    private final CorporationService corporationService;
    private final CorpUpdateJob corpUpdateJob;
    private final UserService userService;
    private final JobSchedulerService jobSchedulerService;


    @PostMapping("/{userId}")
    public CorpDTO newCorp (@PathVariable int userId, @RequestBody int corpId) {
        Corporation corporation = new Corporation();
        corporation.setUser(userService.getUser(userId));
        corporation.setId(corpId);
        corporationService.createNewCorp(corporation);
        corpUpdateJob.updateCorp(corpId);
        jobSchedulerService.scheduleAllJobs();
        return map(corporationService.getCorporation(corpId), CorpDTO.class);
    }

    @GetMapping(value = "/{userId}/{id}")
    @ResponseBody
    public CorpDTO getCorp(@PathVariable int userId, @PathVariable int id) {
        try{
            return map(corporationService.getCorporation(id), CorpDTO.class);
        } catch (EveCharacterNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public void deleteCorp(@PathVariable int id) {
        corporationService.deleteCorp(id);
    }


    @GetMapping(value = "/{userId}")
    @ResponseBody
    public List<CorpDTO> getCharacters(@PathVariable int userId) {
        return mapList(corporationService.getCorporationsForUser(userId), CorpDTO.class);

    }
}
