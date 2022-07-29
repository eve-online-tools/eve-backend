package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.esi.CorpUpdateJob;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.repository.CorporationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporationService {

    private final CorporationRepository corporationRepository;

    public void saveCorp(Corporation c) {
        corporationRepository.save(c);
    }

    public Corporation getCorporation(int corpId) throws EveCharacterNotFoundException {
        Corporation c = corporationRepository.findById(corpId).orElse(null);
        return c;
    }

    public boolean corpExists(int corpId) {
        return corporationRepository.existsById(corpId);
    }

    public void createNewCorp(Corporation corporation) {
        saveCorp(corporation);
    }

    public List<Corporation> getCorporations() {
        return Lists.newArrayList(corporationRepository.findAll());
    }

    public void deleteCorp(int id) {
        corporationRepository.deleteById(id);
    }

    public List<Corporation> getCorporationsForUser(int userId) {
        return Lists.newArrayList(corporationRepository.findByUserId(userId));
    }

    public List<Integer> getCorporationIdsForUser(Integer userId) {
        return corporationRepository.getCorpIdsForUser(userId);
    }
}
