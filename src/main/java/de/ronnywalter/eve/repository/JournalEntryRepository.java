package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.JournalEntry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface JournalEntryRepository extends CrudRepository<JournalEntry, Long> {
    List<JournalEntry> findByCharacterIdInOrCorpIdInOrderByIdDesc(List<Integer> eveCharacterIdsForUser, List<Integer> corporationIdsForUser);
}
