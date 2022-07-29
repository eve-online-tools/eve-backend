package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Type;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface TypeRepository extends PagingAndSortingRepository<Type, Integer> {
    Optional<Type> findByName(String name);
    List<Type> findByPublished(boolean published);

    List<Type> findByMarketGroupId(Integer marketGroupId);
}
