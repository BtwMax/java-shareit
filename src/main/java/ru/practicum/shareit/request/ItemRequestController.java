package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.IncomingItemRequestDto;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.dto.OutLongItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public OutItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                        @Valid @RequestBody IncomingItemRequestDto incomingItemRequestDto) {
        log.info("Запрос на добавление запроса на поиск предмета");
        return itemRequestService.addItemRequest(incomingItemRequestDto, requestorId);
    }

    @GetMapping("/{requestId}")
    public OutLongItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                                @PathVariable("requestId") long requestId) {
        log.info("Запрос на показ запроса по заданному id");
        return itemRequestService.getItemRequestById(requestorId, requestId);
    }

    @GetMapping
    public List<OutLongItemRequestDto> getRequestorItemRequest(@RequestHeader("X-Sharer-User-Id") long requestorId) {
        log.info("Запрос на вывод всех запросов пользователя с id = " + requestorId);
        return itemRequestService.getRequestorItemRequest(requestorId);
    }

    @GetMapping("/all")
    public List<OutLongItemRequestDto> getAllOtherItemRequests(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                                               @RequestParam(required = false) Integer from,
                                                               @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод всех запросов других пользователей");
        return itemRequestService.getAllOtherItemRequest(requestorId, from, size);
    }
}
