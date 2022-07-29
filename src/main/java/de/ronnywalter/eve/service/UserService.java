package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.User;
import de.ronnywalter.eve.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User saveUser(User user) {
        return repository.save(user);
    }

    @Transactional
    public User getUser(int id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteUser(int userId) {
        repository.deleteById(userId);
    }

    public List<User> getUsers() {
        return Lists.newArrayList(repository.findAll());
    }
}
