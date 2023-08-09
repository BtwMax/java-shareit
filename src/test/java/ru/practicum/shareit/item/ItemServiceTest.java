package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.comment.dto.IncomingCommentDto;
import ru.practicum.shareit.item.comment.dto.OutCommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ItemServiceTest {

    @MockBean
    ItemRepository itemRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    CommentRepository commentRepository;
    @MockBean
    BookingRepository bookingRepository;
    @MockBean
    ItemRequestRepository requestRepository;
    @Autowired
    ItemService itemService;

    ItemDto itemDto;

    UserDto userDto;

    OutItemRequestDto request;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .requestId(1L)
                .build();
        request = OutItemRequestDto.builder()
                .id(1L)
                .description("Request")
                .created(LocalDateTime.of(2020, 10, 11, 10, 20))
                .build();
    }

    @Test
    void addValidItemWithoutRequestIdTest() {
        itemDto.setRequestId(null);
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));
        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));

        ItemDto actualItem = itemService.addItem(userDto.getId(), itemDto);

        Assertions.assertEquals(actualItem.getId(), itemDto.getId());
        Assertions.assertEquals(actualItem.getName(), itemDto.getName());
        Assertions.assertEquals(actualItem.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(actualItem.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void addValidItemWithRequestId() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Request")
                .requestor(user2)
                .created(LocalDateTime.of(2020, 10, 11, 10, 20))
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .itemRequest(itemRequest)
                .build();

        when(itemRepository.save(any())).thenReturn(item);
        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));
        when(requestRepository.getReferenceById(itemDto.getRequestId())).thenReturn(itemRequest);

        ItemDto actualItem = itemService.addItem(userDto.getId(), itemDto);
        Assertions.assertEquals(actualItem.getId(), itemDto.getId());
        Assertions.assertEquals(actualItem.getName(), itemDto.getName());
        Assertions.assertEquals(actualItem.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(actualItem.getAvailable(), itemDto.getAvailable());
        Assertions.assertEquals(actualItem.getRequestId(), itemDto.getRequestId());
    }

    @Test
    void addValidItemWithNotFoundUser() {
        long userId = 10L;
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                itemService.addItem(10, itemDto));
        Assertions.assertEquals("Пользователь с id = " + userId + " не найден", exception.getMessage());
    }

    @Test
    void addItemWithNameIsBlank() {
        itemDto.setName(" ");
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации названия", e.getMessage());
    }

    @Test
    void addItemWithNameIsEmpty() {
        itemDto.setName("");
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации названия", e.getMessage());
    }

    @Test
    void addItemWithNameIsNull() {
        itemDto.setName(null);
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации названия", e.getMessage());
    }

    @Test
    void addItemWithDescriptionIsBlank() {
        itemDto.setDescription(" ");
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации описания", e.getMessage());
    }

    @Test
    void addItemWithDescriptionIsEmpty() {
        itemDto.setDescription("");
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации описания", e.getMessage());
    }

    @Test
    void addItemWithDescriptionIsNull() {
        itemDto.setDescription(null);
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации описания", e.getMessage());
    }

    @Test
    void addItemWithAvailableIsNull() {
        itemDto.setAvailable(null);
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
                itemService.addItem(userDto.getId(), itemDto));
        Assertions.assertEquals("Ошибка валидации статуса доступности", e.getMessage());
    }

    @Test
    void updateItemTest() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Request")
                .requestor(user2)
                .created(LocalDateTime.of(2020, 10, 11, 10, 20))
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("UpdateName")
                .description("qwerty")
                .available(false)
                .owner(UserMapper.toUser(userDto))
                .itemRequest(itemRequest)
                .build();

        itemDto.setName("UpdateName");
        itemDto.setDescription("qwerty");
        itemDto.setAvailable(false);
        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.findById(itemDto.getId())).thenReturn(item);
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto actualItem = itemService.updateItem(userDto.getId(), itemDto, itemDto.getId());
        Assertions.assertEquals(actualItem.getId(), itemDto.getId());
        Assertions.assertEquals(actualItem.getName(), itemDto.getName());
        Assertions.assertEquals(actualItem.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(actualItem.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void updateItemWithNotFoundOwner() {
        long userId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemDto, itemDto.getId()));

        Assertions.assertEquals("Нельзя обновить предмет у несуществующего пользователя", e.getMessage());
    }

    @Test
    void updateNotFoundItem() {
        long itemId = 10L;
        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userDto.getId(), itemDto, itemId));

        Assertions.assertEquals("Невозможно обновить несуществующий предмет", e.getMessage());
    }

    @Test
    void updateItemFromUserWithoutThisItems() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("UpdateName")
                .description("qwerty")
                .available(false)
                .owner(UserMapper.toUser(userDto))
                .build();
        when(userRepository.findById(user2.getId())).thenReturn(user2);
        when(itemRepository.findById(item.getId())).thenReturn(item);
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.updateItem(user2.getId(), itemDto, itemDto.getId()));

        Assertions.assertEquals("У пользователя с id = " +
                user2.getId() + " нет такого предмета", e.getMessage());
    }

    @Test
    void testGetItemById() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();

        when(userRepository.findById(user2.getId())).thenReturn(user2);
        when(itemRepository.getItemById(itemDto.getId())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ItemFullDto result = itemService.getItemById(user2.getId(), itemDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), itemDto.getId());
        Assertions.assertEquals(result.getName(), itemDto.getName());
        Assertions.assertEquals(result.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(result.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void testGetItemByIdFromNotOwner() {

        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.getItemById(itemDto.getId())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ItemFullDto result = itemService.getItemById(userDto.getId(), itemDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), itemDto.getId());
        Assertions.assertEquals(result.getName(), itemDto.getName());
        Assertions.assertEquals(result.getDescription(), itemDto.getDescription());
        Assertions.assertEquals(result.getAvailable(), itemDto.getAvailable());
        Assertions.assertNull(result.getLastBooking());
        Assertions.assertNull(result.getNextBooking());
    }

    @Test
    void getItemByNotFoundUserId() {
        long userId = 10L;
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getItemById(userId, itemDto.getId()));
        Assertions.assertEquals("Пользователь с id = " + userId + " не найден", e.getMessage());
    }

    @Test
    void getItemByNotFoundItemId() {
        long itemId = 10L;
        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getItemById(userDto.getId(), itemId));
        Assertions.assertEquals("Предмет с id = " + itemId + " не найден", e.getMessage());
    }

    @Test
    void getUserItemsTest() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("UpdateName")
                .description("qwerty")
                .available(false)
                .owner(UserMapper.toUser(userDto))
                .build();
        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Text")
                .item(item)
                .author(user2)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userDto.getId())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.findAllByOwnerIdOrderById(userDto.getId())).thenReturn(List.of(item));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of(comment1));

        Collection<ItemFullDto> result = itemService.getUserItems(userDto.getId());
        List<Comment> comments = commentRepository.findAllByItem(item);
        Optional<ItemFullDto> fullDto = result.stream().findFirst();
        if (fullDto.isPresent()) {
            ItemFullDto fullDto1 = fullDto.get();
            Assertions.assertEquals(fullDto1.getId(), item.getId());
            Assertions.assertEquals(fullDto1.getName(), item.getName());
            Assertions.assertEquals(fullDto1.getDescription(), item.getDescription());
            Assertions.assertEquals(fullDto1.getAvailable(), item.getAvailable());
            Assertions.assertEquals(fullDto1.getComments().size(), comments.size());
        }
        Assertions.assertEquals(1, result.size());

    }

    @Test
    void getUserItemsWithNotFoundUserTest() {
        long userId = 10L;

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getUserItems(userId));
        Assertions.assertEquals("Пользователь с id = " + userId + " не найден", e.getMessage());
    }

    @Test
    void testFindItemsByText() {
        String searchText = "Item";

        List<Item> items = Collections.singletonList(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        when(itemRepository.findItemsByNameOrDescriptionContainingIgnoreCase(searchText, searchText))
                .thenReturn(items);

        List<ItemDto> result = itemService.findItemsByText(searchText);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(items.get(0).getId(), result.get(0).getId());
        Assertions.assertEquals(items.get(0).getName(), result.get(0).getName());
        Assertions.assertEquals(items.get(0).getDescription(), result.get(0).getDescription());
        Assertions.assertEquals(items.get(0).getAvailable(), result.get(0).getAvailable());
    }

    @Test
    public void testAddComment() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Text")
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .author(user2)
                .created(LocalDateTime.now())
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(user2)
                .status(Status.APPROVED)
                .build();

        IncomingCommentDto incomingCommentDto = IncomingCommentDto.builder()
                .id(1L)
                .text("Text")
                .build();
        List<Booking> bookings = Collections.singletonList(booking);

        when(itemRepository.getItemById(itemDto.getId())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));
        when(userRepository.findById(user2.getId())).thenReturn(user2);
        when(bookingRepository.findFinishedBookingsByItem(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment1);

        OutCommentDto result = itemService.addComment(user2.getId(), itemDto.getId(), incomingCommentDto);
        Assertions.assertEquals(comment1.getId(), result.getId());
        Assertions.assertEquals(comment1.getText(), result.getText());
        Assertions.assertEquals(comment1.getAuthor().getName(), result.getAuthorName());
        Assertions.assertEquals(comment1.getCreated(), result.getCreated());
    }

    @Test
    void addEmptyComment() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        IncomingCommentDto incomingCommentDto = IncomingCommentDto.builder()
                .id(1L)
                .text("")
                .build();

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.addComment(user2.getId(), itemDto.getId(), incomingCommentDto));

        Assertions.assertEquals("Нельзя оставить пустой комментарий", e.getMessage());
    }

    @Test
    void addCommentFromNotFoundItem() {
        long itemId = 10L;
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        IncomingCommentDto incomingCommentDto = IncomingCommentDto.builder()
                .id(1L)
                .text("qwe")
                .build();

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addComment(user2.getId(), itemId, incomingCommentDto));
        Assertions.assertEquals("Предмет с id = " + itemId + " не найден", e.getMessage());
    }

    @Test
    void addCommentFromNotFoundUser() {
        long userId = 10L;
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));
        IncomingCommentDto incomingCommentDto = IncomingCommentDto.builder()
                .id(1L)
                .text("qwe")
                .build();

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemDto.getId(), incomingCommentDto));
        Assertions.assertEquals("Пользователь с id = " + userId + " не найден", e.getMessage());
    }

    @Test
    void addCommentByItemWithEmptyBookingList() {
        User user2 = User.builder()
                .id(2L)
                .name("Userqw")
                .email("userqwe@ya.ru")
                .build();
        IncomingCommentDto incomingCommentDto = IncomingCommentDto.builder()
                .id(1L)
                .text("qwe")
                .build();

        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));
        when(userRepository.findById(anyLong())).thenReturn(user2);
        when(bookingRepository.findFinishedBookingsByItem(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.addComment(user2.getId(), itemDto.getId(), incomingCommentDto));
        Assertions.assertEquals("У пользователя с id = " + user2.getId() +
                " нет завершенной аренды предмета с id = " + itemDto.getId(), e.getMessage());
    }
}
