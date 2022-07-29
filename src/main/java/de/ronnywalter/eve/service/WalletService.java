package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.CharacterWallet;
import de.ronnywalter.eve.model.CorpWallet;
import de.ronnywalter.eve.model.JournalEntry;
import de.ronnywalter.eve.model.Transaction;
import de.ronnywalter.eve.repository.CharacterWalletRepository;
import de.ronnywalter.eve.repository.CorpWalletRepository;
import de.ronnywalter.eve.repository.JournalEntryRepository;
import de.ronnywalter.eve.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final CharacterWalletRepository characterWalletRepository;
    private final CorpWalletRepository corpWalletRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionRepository transactionRepository;

    public CharacterWallet saveCharacterWallet(CharacterWallet w) {
        return characterWalletRepository.save(w);
    }

    public CharacterWallet getCharacterWallet(int characterId) {
        return characterWalletRepository.findById(characterId).orElse(null);
    }

    public CorpWallet saveCorpWallet(CorpWallet w) {
        return corpWalletRepository.save(w);
    }

    public CorpWallet getCorpWallet(int corpId) {
        return corpWalletRepository.findById(corpId).orElse(null);
    }

    public JournalEntry saveJournalEntry(JournalEntry journalEntry) {
        return journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> saveJournalEntries(List<JournalEntry> entries) {
        return Lists.newArrayList(journalEntryRepository.saveAll(entries));
    }

    public Transaction saveTransaction(Transaction t) {
        return transactionRepository.save(t);
    }

    public List<Transaction> saveTransactions(List<Transaction> t) {
        return Lists.newArrayList(transactionRepository.saveAll(t));
    }

    public Set<Long> getTransactionIds() {
        Set<Long> ids = new HashSet<>();
        transactionRepository.findAll().forEach(t -> ids.add(t.getTransactionId()));
        return ids;
    }

    public Set<Long> getJournalIds() {
        Set<Long> ids = new HashSet<>();
        journalEntryRepository.findAll().forEach(t -> ids.add(t.getId()));
        return ids;
    }
}
