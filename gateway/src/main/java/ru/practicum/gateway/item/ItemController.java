package ru.practicum.gateway.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;


@RestController
@Slf4j
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Запрос на добавление предмета");
        return itemClient.addItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable("itemId") long itemId) {
        log.info("Запрос на вывод предмета с id = " + itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long id, @RequestBody ItemDto itemDto) {
        log.info("Запрос на изменение предмета с id = " + id);
        return itemClient.updateItem(userId, id, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на вывод всех предметов пользователя с id = " + userId);
        return itemClient.getUserItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @Valid @RequestParam String text) {
        log.info("Запрос на поиск предметов по тексту в названии или описании");
        if (text == null) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return itemClient.getItemsByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addCommentToItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @PathVariable("itemId") long itemId,
                                                   @Valid @RequestBody IncomingCommentDto incomingCommentDto) {
        log.info("Запрос на добавление комментария к предмету");
        return itemClient.addComment(userId, itemId, incomingCommentDto);
    }
}
