package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@JsonTest
public class UserJsonTest {

    User user;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("name")
                .email("name@ya.ru")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("name@ya.ru")
                .build();
    }

    @Test
    void convertToUserDto() {
        userDto = null;
        userDto = UserMapper.toUserDto(user);
        Assertions.assertEquals(user.getId(), userDto.getId());
        Assertions.assertEquals(user.getName(), userDto.getName());
        Assertions.assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void convertToUser() {
        user = null;
        user = UserMapper.toUser(userDto);
        Assertions.assertEquals(userDto.getId(), user.getId());
        Assertions.assertEquals(userDto.getName(), user.getName());
        Assertions.assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void convertToShortUserDto() {
        ShortUserDto shortUserDto = UserMapper.toShortUserDto(user);
        Assertions.assertEquals(shortUserDto.getId(), user.getId());
        Assertions.assertEquals(shortUserDto.getName(), user.getName());
    }
}
