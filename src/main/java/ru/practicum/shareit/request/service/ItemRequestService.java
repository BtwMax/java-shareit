package ru.practicum.shareit.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.IncomingItemRequestDto;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.dto.OutLongItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestService(ItemRequestRepository requestRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public OutItemRequestDto addItemRequest(IncomingItemRequestDto itemRequestDto, long requestorId) {
        User user = userRepository.findById(requestorId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + requestorId + " не найден");
        }
        LocalDateTime dateTime = LocalDateTime.now();
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(user, itemRequestDto, dateTime);
        ItemRequest itemRequestStorage = requestRepository.save(itemRequest);
        return ItemRequestMapper.toOutItemRequestDto(itemRequestStorage);
    }

    public OutLongItemRequestDto getItemRequestById(long requestorId, long requestId) {
        User user = userRepository.findById(requestorId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + requestorId + " не найден");
        }
        ItemRequest itemRequest = requestRepository.findById(requestId);
        if (itemRequest == null) {
            throw new NotFoundException("Запрос с id = " + requestId + " не найден");
        }
        List<ItemDto> items = itemRepository.findItemsByItemRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
        return ItemRequestMapper.toOutLongItemRequestDto(itemRequest, items);
    }

    public List<OutLongItemRequestDto> getRequestorItemRequest(long requestorId) {
        User user = userRepository.findById(requestorId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + requestorId + " не найден");
        }
        List<ItemRequest> itemRequests = requestRepository.findItemRequestByRequestorIdOrderByCreatedDesc(requestorId);
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(toList());
        List<ItemDto> items = itemRepository.findByItemRequest_IdIn(requestIds).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        /*Теперь в стриме нет обращения к базе*/
        return itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.toOutLongItemRequestDto(itemRequest, items))
                .collect(Collectors.toList());
    }

    public List<OutLongItemRequestDto> getAllOtherItemRequest(long requestorId, Integer from, Integer size) {
        User user = userRepository.findById(requestorId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + requestorId + " не найден");
        }
        List<ItemRequest> itemRequests;
        if (from != null && size != null) {
            if (from < 0 || size <= 0) {
                throw new ValidationException("Ошибка в значениях пагинации: from < 0 или size <= 0");
            }
            Pageable pageable = PageRequest.of(from / size, size);
            itemRequests = requestRepository.findAllOtherItemRequest(requestorId, pageable);
        } else {
            itemRequests = requestRepository.findAllOtherItemRequest(requestorId);
        }
        List<Long> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(toList());
        List<ItemDto> items = itemRepository.findItemsByItemRequestIdIn(requestIds).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        /*тут тоже поправил*/
        return itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.toOutLongItemRequestDto(itemRequest, items))
                .collect(Collectors.toList());
    }
}
