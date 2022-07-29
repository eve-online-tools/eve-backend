package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.TypeGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TypeGroupRepository extends CrudRepository<TypeGroup, Integer> {
    Optional<TypeGroup> findByName(String name);
}
