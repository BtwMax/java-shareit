package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на добавление пользователя");
        return userService.addUser(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") long id) {
        log.info("Запрос на показ пользователя c id = " + id);
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto, @PathVariable("id") long id) {
        log.info("Запрос на обновление пользователя");
        return userService.updateUser(userDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") long id) {
        log.info("Запрос на удаление пользователя с id = " + id);
        userService.removeUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Текущее количество пользователей: {}", userService.getAllUsers().size());
        return userService.getAllUsers();
    }
}
