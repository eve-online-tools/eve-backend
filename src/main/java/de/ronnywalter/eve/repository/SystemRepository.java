package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.SolarSystem;
import org.springframework.data.repository.CrudRepository;


public interface SystemRepository extends CrudRepository<SolarSystem, Integer> {
}
