package ru.practicum.gateway.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {

    private final RequestClient requestClient;

    @Autowired
    public ItemRequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                             @Valid @RequestBody IncomingItemRequestDto incomingItemRequestDto) {
        log.info("Запрос на добавление запроса на поиск предмета");
        return requestClient.addRequest(requestorId, incomingItemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                                 @PathVariable("requestId") long requestId) {
        log.info("Запрос на показ запроса по заданному id");
        return requestClient.getItemRequestById(requestorId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestorItemRequest(@RequestHeader("X-Sharer-User-Id") long requestorId) {
        log.info("Запрос на вывод всех запросов пользователя с id = " + requestorId);
        return requestClient.getRequestorItemRequest(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherItemRequests(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                                          @RequestParam(required = false) Integer from,
                                                          @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод всех запросов других пользователей");
        return requestClient.getAllOtherItemRequests(requestorId, from, size);
    }
}
