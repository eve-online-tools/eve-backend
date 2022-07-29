package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.TradeCandidate;
import de.ronnywalter.eve.model.TradeCandidateId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeCandidateRepository extends JpaRepository<TradeCandidate, TradeCandidateId> {
}