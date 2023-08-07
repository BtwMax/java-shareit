package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @Test
    void addCorrectUserTest() {
        UserDto userDto = UserDto.builder()
                .name("User")
                .email("User@ya.ru")
                .build();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("User@ya.ru")
                .build();
        UserDto outUserDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("User@ya.ru")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto actualUser = userService.addUser(userDto);

        Assertions.assertEquals(actualUser.getId(), outUserDto.getId());
        Assertions.assertEquals(actualUser.getName(), outUserDto.getName());
        Assertions.assertEquals(actualUser.getEmail(), outUserDto.getEmail());
    }

    @Test
    void addUserWithEmptyEmailTest() {
        UserDto userDto = UserDto.builder()
                .name("user")
                .email("")
                .build();
        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                userService.addUser(userDto));
        Assertions.assertEquals(e.getMessage(), "Ошибка валидации Email");
    }

    @Test
    void addUserWithEmailIsBlankTest() {
        UserDto userDto = UserDto.builder()
                .name("user")
                .email(" ")
                .build();
        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                userService.addUser(userDto));
        Assertions.assertEquals(e.getMessage(), "Ошибка валидации Email");
    }

    @Test
    void updateUserTest() {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("UserUp")
                .email("UserUp@ya.ru")
                .build();
        User user = User.builder()
                .id(userId)
                .name("User")
                .email("User@ya.ru")
                .build();
        UserDto outDtoUser = UserDto.builder()
                .id(userId)
                .name("UserUp")
                .email("UserUp@ya.ru")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findById(userId)).thenReturn(user);

        UserDto updateActualUser = userService.updateUser(userDto, userId);

        Assertions.assertEquals(updateActualUser.getId(), outDtoUser.getId());
        Assertions.assertEquals(updateActualUser.getName(), outDtoUser.getName());
        Assertions.assertEquals(updateActualUser.getEmail(), outDtoUser.getEmail());
    }

    @Test
    void updateUserNameTest() {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("UserUp")
                .build();
        User user = User.builder()
                .id(userId)
                .name("User")
                .email("User@ya.ru")
                .build();
        UserDto outDtoUser = UserDto.builder()
                .id(userId)
                .name("UserUp")
                .email("User@ya.ru")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findById(anyLong())).thenReturn(user);

        UserDto updateActualUser = userService.updateUser(userDto, userId);

        Assertions.assertEquals(updateActualUser.getId(), outDtoUser.getId());
        Assertions.assertEquals(updateActualUser.getName(), outDtoUser.getName());
        Assertions.assertEquals(updateActualUser.getEmail(), outDtoUser.getEmail());
    }

    @Test
    void updateUserEmailTest() {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .email("UserUp@ya.ru")
                .build();
        User user = User.builder()
                .id(userId)
                .name("User")
                .email("User@ya.ru")
                .build();
        UserDto outDtoUser = UserDto.builder()
                .id(userId)
                .name("User")
                .email("UserUp@ya.ru")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findById(anyLong())).thenReturn(user);

        UserDto updateActualUser = userService.updateUser(userDto, userId);

        Assertions.assertEquals(updateActualUser.getId(), outDtoUser.getId());
        Assertions.assertEquals(updateActualUser.getName(), outDtoUser.getName());
        Assertions.assertEquals(updateActualUser.getEmail(), outDtoUser.getEmail());
    }

    @Test
    void updateNotFoundUserTest() {
        long findUserId = 10;
        UserDto userDto = UserDto.builder()
                .email("User@ya.ru")
                .build();
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.updateUser(userDto, findUserId));
        Assertions.assertEquals(exception.getMessage(), "Невозможно обновить несуществующего пользователя");
    }

    @Test
    void getUserByIdTest() {
        long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("User")
                .email("User@ya.ru")
                .build();
        UserDto outDtoUser = UserDto.builder()
                .id(userId)
                .name("User")
                .email("User@ya.ru")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(user);

        UserDto foundUser = userService.getUserById(userId);

        Assertions.assertEquals(foundUser.getId(), outDtoUser.getId());
        Assertions.assertEquals(foundUser.getName(), outDtoUser.getName());
        Assertions.assertEquals(foundUser.getEmail(), outDtoUser.getEmail());
    }

    @Test
    void getUserByNotFoundIdTest() {
        long findUserId = 10;
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.getUserById(findUserId));
        Assertions.assertEquals(exception.getMessage(), "Пользователь с id = " + findUserId + " не найден");
    }

    @Test
    void deleteUserByIdTest() {
        long userId = 1L;
        userService.removeUser(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void getAllUsersListTest() {
        User user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@yandex.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@yandex.ru")
                .build();
        List<User> usersStorage = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(usersStorage);

        List<UserDto> actualUsersDto = userService.getAllUsers();

        Assertions.assertEquals(actualUsersDto.size(), usersStorage.size());
        Assertions.assertEquals(usersStorage.get(0).getId(), actualUsersDto.get(0).getId());
        Assertions.assertEquals(usersStorage.get(0).getName(), actualUsersDto.get(0).getName());
        Assertions.assertEquals(usersStorage.get(0).getEmail(), actualUsersDto.get(0).getEmail());
        Assertions.assertEquals(usersStorage.get(1).getId(), actualUsersDto.get(1).getId());
        Assertions.assertEquals(usersStorage.get(1).getName(), actualUsersDto.get(1).getName());
        Assertions.assertEquals(usersStorage.get(1).getEmail(), actualUsersDto.get(1).getEmail());
    }
}
