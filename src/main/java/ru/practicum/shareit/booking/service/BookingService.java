package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.IncomingBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServerErrorException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                          ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public BookingDto addBooking(long bookerId, IncomingBookingDto incomingBookingDto) {
        User user = userRepository.findById(bookerId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + bookerId + " не найден");
        }

        Item item = itemRepository.getItemById(incomingBookingDto.getItemId());
        if (item == null) {
            throw new NotFoundException("Предмет с id = " + incomingBookingDto.getItemId() +
                    " у пользоватея не найден");
        }
        if (item.getOwner().getId() == bookerId) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }
        if (incomingBookingDto.getStart().isAfter(incomingBookingDto.getEnd())) {
            throw new ValidationException("Ошибка со временем аренды: время конца не должно быть раньше времени начала");
        }
        if (incomingBookingDto.getStart().isEqual(incomingBookingDto.getEnd())) {
            throw new ValidationException("Время старта и конца оренды должно различаться");
        }
        if (!item.getAvailable()) {
            throw new AvailableException("Предмет с id = " + incomingBookingDto.getItemId() +
                    " недоступен для бронирования");
        }
        Booking booking = BookingMapper.toBooking(incomingBookingDto, item, user, Status.WAITING);
        Booking bookingStorage = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(bookingStorage, ItemMapper.toItemDto(item), UserMapper.toShortUserDto(user));
    }

    public BookingDto changeApproveStatus(long ownerId, long bookingId, boolean approved) {
        User user = userRepository.findById(ownerId);
        if (user == null) {
            throw new NotFoundException("Арендодатель с id = " + ownerId + " не найден");
        }
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Запрос с id = " + ownerId + " не найден");
        }
        if (booking.getStatus() == Status.APPROVED) {
            throw new ValidationException("Статус бронирования уже подтвержден");
        }
        Item item = booking.getItem();
        if (item.getOwner().getId() != user.getId()) {
            throw new NotFoundException("У пользователя с id = " + user.getId() + " не найден такой предмет");
        }
        User booker = booking.getBooker();
        if (booker == null) {
            throw new NotFoundException("Арендатор не найден");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking bookingStorage = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(bookingStorage, ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toShortUserDto(booker));
    }

    public BookingDto getBookingById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Запрос с id = " + bookingId + " не найден");
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Item item = booking.getItem();
        if (userId != booking.getBooker().getId() && userId != item.getOwner().getId()) {
            throw new NotFoundException("Пользователь не является арендодателем или арендатором");
        }
        return BookingMapper.toBookingDto(booking, ItemMapper.toItemDto(item), UserMapper.toShortUserDto(booking.getBooker()));
    }

    public List<BookingDto> getBookingByBooker(long bookerId, String state) {
        User user = userRepository.findById(bookerId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + bookerId + " не найден");
        }
        List<Booking> bookings = new ArrayList<>();

        try {
            switch (State.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerCurrent(bookerId, LocalDateTime.now());
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, Status.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, Status.REJECTED);
                    break;

            }
        } catch (IllegalArgumentException e) {
            throw new ServerErrorException("Unknown state: " + state);
        }
        return toListBookingDto(bookings);
    }

    public List<BookingDto> getBookingByOwner(long ownerId, String state) {
        User user = userRepository.findById(ownerId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не найден");
        }
        List<Booking> bookings = new ArrayList<>();

        try {
            switch (State.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findAllBookingByOwner(ownerId);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findCurrentBookingByOwner(ownerId, LocalDateTime.now());
                    break;
                case PAST:
                    bookings = bookingRepository.findPastBookingByOwner(ownerId, LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findFutureBookingByOwner(ownerId, LocalDateTime.now());
                    break;
                case WAITING:
                    bookings = bookingRepository.findBookingByOwnerIdAndStatus(ownerId, Status.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findBookingByOwnerIdAndStatus(ownerId, Status.REJECTED);
                    break;

            }
        } catch (IllegalArgumentException e) {
            throw new ServerErrorException("Unknown state: " + state);
        }
        return toListBookingDto(bookings);
    }

    private List<BookingDto> toListBookingDto(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(
                        booking,
                        ItemMapper.toItemDto(booking.getItem()),
                        UserMapper.toShortUserDto(booking.getBooker())
                ))
                .collect(Collectors.toList());
    }
}
