package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.dto.EveCharacterDTO;
import de.ronnywalter.eve.dto.TransactionDTO;
import de.ronnywalter.eve.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionsController extends AbstractController {

    private final TransactionService transactionService;


    @GetMapping(value = "/{userId}")
    @ResponseBody
    public List<TransactionDTO> getTransactions(@PathVariable int userId) {
        return mapList(transactionService.getTransactions(userId), TransactionDTO.class);
    }

}
