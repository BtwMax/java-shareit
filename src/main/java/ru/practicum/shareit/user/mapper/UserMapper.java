package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public class UserMapper {

    public static UserDto toUserDto(long id, User user) {
        return UserDto.builder()
                .id(id)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User toUser(long id, UserDto userDto) {
        return User.builder()
                .id(id)
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }
}
