package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.IsExistException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @Override
    public User addUser(User user) {
        validate(user);
        checkEmail(user);
        if (user.getId() == 0) {
            user.setId(generatorId());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Невозможно обновить несуществующего пользователя");
        }
        User updateUser = getUserById(user.getId());
        if (user.getEmail() == null) {
            user.setEmail(updateUser.getEmail());
        }
        if (user.getName() == null) {
            user.setName(updateUser.getName());
            if (!updateUser.getEmail().equals(user.getEmail())) {
                checkEmail(user);
            }
        }
        validate(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public void removeUser(long id) {
        users.remove(id);
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private long generatorId() {
        return id++;
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().equals("") || user.getEmail().isBlank() ||
                user.getEmail().isEmpty()) {
            throw new ValidationException("Ошибка валидации Email");
        }
    }

    private void checkEmail(User user) {
        String email = user.getEmail();
        for (User user1 : users.values()) {
            if (user1.getEmail().equals(email)) {
                throw new IsExistException("Пользователь с таким email уже существует");
            }
        }
    }
}
