package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.User;
import de.ronnywalter.eve.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;

    public void saveCharacter(EveCharacter c) {
        characterRepository.save(c);
    }

    public EveCharacter getEveCharacter(int characterId) {
        EveCharacter eveCharacter = characterRepository.findById(characterId).orElse(null);
        return eveCharacter;
    }

    public EveCharacter getEveCharacter(String name) {
        EveCharacter eveCharacter = characterRepository.findByName(name).orElse(null);
        return eveCharacter;
    }

    public boolean characterExists(int characterId) {
        return characterRepository.existsById(characterId);
    }

    public List<EveCharacter> getEveCharactersForUser(Integer userId) {
        return Lists.newArrayList(characterRepository.findByUserId(userId));
    }

    public List<Integer> getEveCharacterIdsForUser(Integer userId) {
        return characterRepository.getCharacterIdsForUser(userId);
    }

    public List<EveCharacter> getEveCharacters() {
        return Lists.newArrayList(characterRepository.findAll());
    }

    public void deleteCharacter(int id) {
        characterRepository.deleteById(id);
    }

    public EveCharacter getEveCharacterForUser(int userId, int id) {
        return characterRepository.findByIdAndUserId(id, userId);
    }
}
