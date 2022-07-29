package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.JournalDTO;
import de.ronnywalter.eve.dto.TransactionDTO;
import de.ronnywalter.eve.service.JournalService;
import de.ronnywalter.eve.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("journal")
@RequiredArgsConstructor
@Slf4j
public class JournalController extends AbstractController {

    private final JournalService journalService;


    @GetMapping(value = "/{userId}")
    @ResponseBody
    public List<JournalDTO> getJournal(@PathVariable int userId) {
        return mapList(journalService.getJournalForUser(userId), JournalDTO.class);
    }
}
