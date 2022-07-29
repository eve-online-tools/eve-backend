package de.ronnywalter.eve.service;

import de.ronnywalter.eve.model.TradeCandidate;
import de.ronnywalter.eve.repository.TradeCandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeCandidateService {

    private final TradeCandidateRepository tradeCandidateRepository;

    public List<TradeCandidate> saveTradeCandidates(List<TradeCandidate> candidates) {
        return tradeCandidateRepository.saveAll(candidates);
    }
}
