package ru.practicum.gateway.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на добавление пользователя");
        return userClient.addUser(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") long id) {
        log.info("Запрос на показ пользователя c id = " + id);
        return userClient.getUserById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto, @PathVariable("id") long id) {
        log.info("Запрос на обновление пользователя");
        return userClient.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") long id) {
        log.info("Запрос на удаление пользователя с id = " + id);
        return userClient.delUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Текущие пользователи: {}", userClient.getUsers());
        return userClient.getUsers();
    }
}
