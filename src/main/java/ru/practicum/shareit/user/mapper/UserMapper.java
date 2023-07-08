package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    public UserDto toUserDto(long id, User user) {
        return UserDto.builder()
                .id(id)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public User toUser(long id, UserDto userDto) {
        return User.builder()
                .id(id)
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }
}
