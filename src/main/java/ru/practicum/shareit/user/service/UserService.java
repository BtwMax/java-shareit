package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;


@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validate(user);
        User userStorage = userRepository.save(user);
        return UserMapper.toUserDto(userStorage);
    }

    @Transactional
    public UserDto updateUser(UserDto userDto, long id) {
        User user = UserMapper.toUser(userDto);
        User updateUser = userRepository.findById(id);
        if (updateUser == null) {
            throw new NotFoundException("Невозможно обновить несуществующего пользователя");
        }
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            updateUser.setName(user.getName());
        }
        validate(updateUser);
        User userStorage = userRepository.save(updateUser);
        return UserMapper.toUserDto(userStorage);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    public void removeUser(long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().equals("") || user.getEmail().isBlank() ||
                user.getEmail().isEmpty()) {
            throw new ValidationException("Ошибка валидации Email");
        }
    }
}
