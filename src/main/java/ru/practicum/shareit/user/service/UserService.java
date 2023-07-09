package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto.getId(), userDto);
        User userStorage = userRepository.addUser(user);
        return UserMapper.toUserDto(userStorage.getId(), userStorage);
    }

    public UserDto updateUser(long id, UserDto userDto) {
        User user = UserMapper.toUser(id, userDto);
        User userStorage = userRepository.updateUser(user);
        return UserMapper.toUserDto(userStorage.getId(), userStorage);
    }

    public UserDto getUserById(long id) {
        User user = userRepository.getUserById(id);
        return UserMapper.toUserDto(user.getId(), user);
    }

    public void removeUser(long id) {
        userRepository.removeUser(id);
    }

    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(user -> UserMapper.toUserDto(user.getId(), user))
                .collect(Collectors.toList());
    }
}
