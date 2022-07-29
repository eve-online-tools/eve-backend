package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findByOrderByTransactionIdDesc();

    List<Transaction> findByCharacterIdInOrCorpIdInOrderByTransactionIdDesc(List<Integer> charIds, List<Integer> corpIds);
}
