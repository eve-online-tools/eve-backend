package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.EveCharacter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CorporationRepository extends CrudRepository<Corporation, Integer> {
    List<Corporation>  findByUserId(int userId);

    @Query("select c.id from #{#entityName} c where c.user.id = ?1")
    List<Integer> getCorpIdsForUser(Integer userId);
}
