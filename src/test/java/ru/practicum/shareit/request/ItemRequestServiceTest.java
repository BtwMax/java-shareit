package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.IncomingItemRequestDto;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.dto.OutLongItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ItemRequestServiceTest {

    @MockBean
    ItemRequestRepository requestRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    ItemRepository itemRepository;

    @Autowired
    ItemRequestService itemRequestService;

    UserDto userDto;

    UserDto userDto2;

    OutItemRequestDto outRequestDto;

    IncomingItemRequestDto itemRequestDto;

    ItemDto itemDto;

    OutLongItemRequestDto longItemRequestDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .email("Email@ya.ru")
                .name("User")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .email("Email2@ya.ru")
                .name("User2")
                .build();
        outRequestDto = OutItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2023, 10, 10, 12, 10, 15))
                .build();
        itemRequestDto = IncomingItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();
        longItemRequestDto = OutLongItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2023, 10, 10, 12, 10, 15))
                .items(List.of(itemDto))
                .build();
    }

    @Test
    void addItemRequestWithRequestorIsNotFound() {
        long requestorId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.addItemRequest(itemRequestDto, requestorId));
        Assertions.assertEquals("Пользователь с id = " + requestorId + " не найден", e.getMessage());
    }

    @Test
    void addItemRequestTest() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(requestRepository.save(any()))
                .thenReturn(ItemRequestMapper.toItemRequest(UserMapper.toUser(userDto),
                        itemRequestDto, outRequestDto.getCreated()));

        OutItemRequestDto actualRequestDto = itemRequestService.addItemRequest(itemRequestDto, userDto.getId());

        Assertions.assertEquals(actualRequestDto.getId(), outRequestDto.getId());
        Assertions.assertEquals(actualRequestDto.getDescription(), outRequestDto.getDescription());
        Assertions.assertEquals(actualRequestDto.getCreated(), outRequestDto.getCreated());
    }

    @Test
    void getItemByIdWithRequestorIsNotFound() {
        long requestorId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(requestorId, outRequestDto.getId()));
        Assertions.assertEquals("Пользователь с id = " + requestorId + " не найден", e.getMessage());
    }

    @Test
    void getItemByIdWithRequestIsNotFound() {
        long requestId = 10L;
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userDto.getId(), requestId));
        Assertions.assertEquals("Запрос с id = " + requestId + " не найден", e.getMessage());
    }

    @Test
    void getItemRequestByIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(requestRepository.findById(anyLong()))
                .thenReturn(ItemRequestMapper.toItemRequest(UserMapper.toUser(userDto),
                        itemRequestDto, outRequestDto.getCreated()));
        List<Item> items = List.of(ItemMapper.toItem(UserMapper.toUser(userDto2), itemDto));
        when(itemRepository.findItemsByItemRequestId(anyLong())).thenReturn(items);

        OutLongItemRequestDto actualRequestDto = itemRequestService.getItemRequestById(userDto.getId(), longItemRequestDto.getId());
        Assertions.assertEquals(actualRequestDto.getId(), longItemRequestDto.getId());
        Assertions.assertEquals(actualRequestDto.getDescription(), longItemRequestDto.getDescription());
        Assertions.assertEquals(actualRequestDto.getCreated(), longItemRequestDto.getCreated());
        Assertions.assertEquals(actualRequestDto.getItems().get(0).getId(), longItemRequestDto.getItems().get(0).getId());
    }

    @Test
    void getAllItemRequestWithRequestorIsNotFound() {
        long requestorId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestorItemRequest(requestorId));

        Assertions.assertEquals("Пользователь с id = " + requestorId + " не найден", e.getMessage());
    }

    @Test
    void getAllRequestorItemRequests() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        List<ItemRequest> itemRequests = List.of(ItemRequestMapper.toItemRequest(UserMapper.toUser(userDto),
                itemRequestDto, outRequestDto.getCreated()));
        when(requestRepository.findItemRequestByRequestorIdOrderByCreatedDesc(anyLong())).thenReturn(itemRequests);
        List<Item> items = List.of(ItemMapper.toItem(UserMapper.toUser(userDto2), itemDto));
        when(itemRepository.findByItemRequest_IdIn(any())).thenReturn(items);

        List<OutLongItemRequestDto> actualList = itemRequestService.getRequestorItemRequest(userDto.getId());
        Assertions.assertEquals(actualList.size(), 1);
        Assertions.assertEquals(actualList.get(0).getId(), longItemRequestDto.getId());
        Assertions.assertEquals(actualList.get(0).getDescription(), longItemRequestDto.getDescription());
        Assertions.assertEquals(actualList.get(0).getItems().get(0).getId(), longItemRequestDto.getItems().get(0).getId());
        Assertions.assertEquals(actualList.get(0).getCreated(), longItemRequestDto.getCreated());
    }

    @Test
    void getAllOtherItemRequestWithUserNotFound() {
        long requestorId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllOtherItemRequest(requestorId, null, null));

        Assertions.assertEquals("Пользователь с id = " + requestorId + " не найден", e.getMessage());
    }

    @Test
    void getAllOtherItemRequestWithInvalidPagination() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemRequestService.getAllOtherItemRequest(userDto.getId(), 0, 0));
        Assertions.assertEquals("Ошибка в значениях пагинации: from < 0 или size <= 0", e.getMessage());
    }

    @Test
    void getAllOtherItemRequestWithCorrectPagination() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        List<ItemRequest> itemRequests = List.of(ItemRequestMapper.toItemRequest(UserMapper.toUser(userDto),
                itemRequestDto, outRequestDto.getCreated()));
        when(requestRepository.findAllOtherItemRequest(anyLong(), any())).thenReturn(itemRequests);
        when(itemRepository.findItemsByItemRequestIdIn(any()))
                .thenReturn(List.of(ItemMapper.toItem(UserMapper.toUser(userDto2), itemDto)));

        List<OutLongItemRequestDto> actualList = itemRequestService.getAllOtherItemRequest(userDto.getId(), 0, 1);
        Assertions.assertEquals(actualList.size(), 1);
        Assertions.assertEquals(actualList.get(0).getId(), longItemRequestDto.getId());
        Assertions.assertEquals(actualList.get(0).getDescription(), longItemRequestDto.getDescription());
        Assertions.assertEquals(actualList.get(0).getItems().get(0).getId(), longItemRequestDto.getItems().get(0).getId());
        Assertions.assertEquals(actualList.get(0).getCreated(), longItemRequestDto.getCreated());
    }

    @Test
    void getAllOtherItemRequestWithoutPagination() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        List<ItemRequest> itemRequests = List.of(ItemRequestMapper.toItemRequest(UserMapper.toUser(userDto),
                itemRequestDto, outRequestDto.getCreated()));
        when(requestRepository.findAllOtherItemRequest(anyLong())).thenReturn(itemRequests);
        List<Item> items = List.of(ItemMapper.toItem(UserMapper.toUser(userDto2), itemDto));
        when(itemRepository.findItemsByItemRequestIdIn(any())).thenReturn(items);

        List<OutLongItemRequestDto> actualList = itemRequestService.getAllOtherItemRequest(userDto.getId(), null, null);
        Assertions.assertEquals(actualList.size(), 1);
        Assertions.assertEquals(actualList.get(0).getId(), longItemRequestDto.getId());
        Assertions.assertEquals(actualList.get(0).getDescription(), longItemRequestDto.getDescription());
        Assertions.assertEquals(actualList.get(0).getItems().get(0).getId(), longItemRequestDto.getItems().get(0).getId());
        Assertions.assertEquals(actualList.get(0).getCreated(), longItemRequestDto.getCreated());
    }
}
