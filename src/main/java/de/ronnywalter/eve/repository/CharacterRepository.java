package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.EveCharacter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends PagingAndSortingRepository<EveCharacter, Integer> {
    Optional<EveCharacter> findByName(String name);
    List<EveCharacter> findByUserId(int id);

    @Query("select c.id from #{#entityName} c where c.user.id = ?1")
    List<Integer> getCharacterIdsForUser(Integer userId);

    EveCharacter findByIdAndUserId(int id, int userId);
}
