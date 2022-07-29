package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

    User findByName(String username);
}