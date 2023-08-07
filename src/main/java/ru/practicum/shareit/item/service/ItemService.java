package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.comment.dto.IncomingCommentDto;
import ru.practicum.shareit.item.comment.dto.OutCommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository,
                       BookingRepository bookingRepository, CommentRepository commentRepository,
                       ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Transactional
    public ItemDto addItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(userRepository.findById(userId), itemDto);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.getReferenceById(itemDto.getRequestId());
            item.setItemRequest(itemRequest);
        }
        validate(item);
        if (userRepository.findById(userId) == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Item itemStorage = itemRepository.save(item);
        return ItemMapper.toItemDto(itemStorage);
    }

    @Transactional(readOnly = true)
    public ItemFullDto getItemById(long userId, long itemId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с id = " + itemId + " не найден");
        }
        ItemFullDto itemFullDto;
        if (userId == item.getOwner().getId()) {
            LocalDateTime dateTime = LocalDateTime.now();
            itemFullDto = ItemMapper.toItemFullDto(item, getLastItemBooking(item, dateTime),
                    getNextItemBooking(item, dateTime), getItemComments(item));
        } else {
            itemFullDto = ItemMapper.toItemFullDto(item, null, null, getItemComments(item));
        }
        return itemFullDto;
    }

    @Transactional
    public ItemDto updateItem(long userId, ItemDto itemDto, long id) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("Нельзя обновить предмет у несуществующего пользователя");
        }
        Item item = ItemMapper.toItem(userRepository.findById(userId), itemDto);
        Item itemIsExist = itemRepository.findById(id);
        if (itemIsExist == null) {
            throw new NotFoundException("Невозможно обновить несуществующий предмет");
        }
        Item updateItem = itemRepository.findById(id);
        long ownerId = updateItem.getOwner().getId();
        if (ownerId != item.getOwner().getId()) {
            throw new NotFoundException("У пользователя с id = " + item.getOwner().getId() + " нет такого предмета");
        }
        if (item.getName() != null) {
            updateItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        validate(updateItem);
        Item itemStorage = itemRepository.save(updateItem);
        return ItemMapper.toItemDto(itemStorage);
    }

    @Transactional(readOnly = true)
    public Collection<ItemFullDto> getUserItems(long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        LocalDateTime dateTime = LocalDateTime.now();
        return itemRepository.findAllByOwnerIdOrderById(id).stream()
                .map(item -> ItemMapper.toItemFullDto(item, getLastItemBooking(item, dateTime),
                        getNextItemBooking(item, dateTime), getItemComments(item)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findItemsByText(String text) {
        return itemRepository.findItemsByNameOrDescriptionContainingIgnoreCase(text, text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OutCommentDto addComment(long userId, long itemId, IncomingCommentDto incomingCommentDto) {
        if (incomingCommentDto.getText().isEmpty()) {
            throw new ValidationException("Нельзя оставить пустой комментарий");
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с id = " + itemId + " не найден");
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findFinishedBookingsByItem(itemId, userId, dateTime);
        if (bookings == null || bookings.size() == 0) {
            throw new ValidationException("У пользователя с id = " + userId +
                    " нет завершенной аренды предмета с id = " + itemId);
        }
        Comment comment = CommentMapper.toComment(incomingCommentDto, item, user, dateTime);
        Comment commentStorage = commentRepository.save(comment);
        return CommentMapper.toOutCommentDto(commentStorage);
    }

    private BookingForItemDto getLastItemBooking(Item item, LocalDateTime dateTime) {
        BookingForItemDto lastBooking = null;
        Optional<Booking> optionalBooking = bookingRepository.findItemLastBooking(item.getId(), dateTime);
        if (optionalBooking.isPresent()) {
            lastBooking = BookingMapper.toBookingForItemDto(optionalBooking.get());
        }
        return lastBooking;
    }

    private BookingForItemDto getNextItemBooking(Item item, LocalDateTime dateTime) {
        BookingForItemDto nextBooking = null;
        Optional<Booking> optionalBooking = bookingRepository.findItemNextBooking(item.getId(), dateTime);
        if (optionalBooking.isPresent()) {
            nextBooking = BookingMapper.toBookingForItemDto(optionalBooking.get());
        }
        return nextBooking;
    }

    private List<OutCommentDto> getItemComments(Item item) {
        List<Comment> comments = commentRepository.findAllByItem(item);
        if (comments != null && !comments.isEmpty()) {
            return comments.stream()
                    .map(CommentMapper::toOutCommentDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void validate(Item item) {
        if (item.getName() == null || item.getName().isBlank() || item.getName().isEmpty()) {
            throw new ValidationException("Ошибка валидации названия");
        }
        if (item.getDescription() == null || item.getDescription().isBlank() || item.getDescription().isEmpty()) {
            throw new ValidationException("Ошибка валидации описания");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Ошибка валидации статуса доступности");
        }
    }
}
