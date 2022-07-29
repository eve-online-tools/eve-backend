package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface RegionRepository extends CrudRepository<Region, Integer> {
    Optional<Region> findByName(String name);
}
