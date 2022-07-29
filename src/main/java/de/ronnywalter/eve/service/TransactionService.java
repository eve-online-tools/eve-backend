package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.Transaction;
import de.ronnywalter.eve.model.User;
import de.ronnywalter.eve.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CharacterService characterService;
    private final CorporationService corporationService;

    public Transaction saveTransaction(Transaction t) {
        return transactionRepository.save(t);
    }

    public List<Transaction> saveTransactions(List<Transaction> t) {
        return Lists.newArrayList(transactionRepository.saveAll(t));
    }

    public Set<Long> getIds() {
        Set<Long> ids = new HashSet<>();
        transactionRepository.findAll().forEach(t -> ids.add(t.getTransactionId()));
        return ids;
    }

    public List<Transaction> getTransactions(int userId) {
        return transactionRepository.findByCharacterIdInOrCorpIdInOrderByTransactionIdDesc(
                characterService.getEveCharacterIdsForUser(userId),
                corporationService.getCorporationIdsForUser(userId));
    }
}
