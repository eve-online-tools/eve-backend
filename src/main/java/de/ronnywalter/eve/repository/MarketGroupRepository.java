package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.MarketGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MarketGroupRepository extends CrudRepository<MarketGroup, Integer> {

    List<MarketGroup> findByParentIdOrderByName(Integer parentId);

}
