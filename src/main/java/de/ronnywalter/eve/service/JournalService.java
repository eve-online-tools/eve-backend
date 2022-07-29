package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.JournalEntry;
import de.ronnywalter.eve.model.Transaction;
import de.ronnywalter.eve.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final CharacterService characterService;
    private final CorporationService corporationService;

    public JournalEntry saveJournalEntry(JournalEntry journalEntry) {
        return journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> saveJournalEntries(List<JournalEntry> entries) {
        return Lists.newArrayList(journalEntryRepository.saveAll(entries));
    }

    public List<JournalEntry> getJournalForUser(int userId) {
        return journalEntryRepository.findByCharacterIdInOrCorpIdInOrderByIdDesc(
                characterService.getEveCharacterIdsForUser(userId),
                corporationService.getCorporationIdsForUser(userId));
    }

}
