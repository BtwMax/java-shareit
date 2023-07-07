package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    User addUser(User user);

    User updateUser(User user);

    User getUserById(long id);

    void removeUser(long id);

    Collection<User> getAllUsers();
}
