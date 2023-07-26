package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.IncomingCommentDto;
import ru.practicum.shareit.item.comment.dto.OutCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;


@RestController
@Slf4j
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto itemDto) {
        log.info("Запрос на добавление предмета");
        return itemService.addItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemFullDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable("itemId") long itemId) {
        log.info("Запрос на вывод предмета с id = " + itemId);
        return itemService.getItemById(userId, itemId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long id, @RequestBody ItemDto itemDto) {
        log.info("Запрос на изменение предмета с id = " + id);
        return itemService.updateItem(userId, itemDto, id);
    }

    @GetMapping
    public Collection<ItemFullDto> getUserItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на вывод всех предметов пользователя с id = " + userId);
        return itemService.getUserItems(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemsByText(@RequestParam String text) {
        log.info("Запрос на поиск предметов по тексту в названии или описании");
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemService.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public OutCommentDto addCommentToItem(@Valid @RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable("itemId") long itemId,
                                          @RequestBody IncomingCommentDto incomingCommentDto) {
        log.info("Запрос на добавление комментария к предмету");
        return itemService.addComment(userId, itemId, incomingCommentDto);
    }
}
