package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.TypeCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TypeCategorityRepository extends CrudRepository<TypeCategory, Integer> {
    Optional<TypeCategory> findByName(String name);
}
