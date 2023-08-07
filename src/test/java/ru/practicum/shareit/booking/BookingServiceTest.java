package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.IncomingBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.AvailableException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BookingServiceTest {

    @MockBean
    BookingRepository bookingRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    ItemRepository itemRepository;

    @Autowired
    BookingService bookingService;

    BookingDto bookingDto;

    ItemDto itemDto;

    UserDto userDto;

    UserDto userDto2;

    IncomingBookingDto incomingBookingDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .email("Email@ya.ru")
                .name("User")
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        userDto2 = UserDto.builder()
                .id(2L)
                .email("Email2@ya.ru")
                .name("User2")
                .build();

        bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2022, 12, 12, 12, 12, 12))
                .end(LocalDateTime.of(2022, 12, 13, 12, 12, 12))
                .item(itemDto)
                .booker(UserMapper.toShortUserDto(UserMapper.toUser(userDto2)))
                .status(Status.WAITING)
                .build();

        incomingBookingDto = IncomingBookingDto.builder()
                .itemId(itemDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    @Test
    void addBookingTest() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));
        when(bookingRepository.save(any()))
                .thenReturn(BookingMapper.toBooking(incomingBookingDto, ItemMapper.toItem(UserMapper.toUser(userDto), itemDto),
                        UserMapper.toUser(userDto2), Status.WAITING));

        BookingDto actualBooking = bookingService.addBooking(userDto2.getId(), incomingBookingDto);

        Assertions.assertEquals(actualBooking.getId(), bookingDto.getId());
        Assertions.assertEquals(actualBooking.getStart(), bookingDto.getStart());
        Assertions.assertEquals(actualBooking.getEnd(), bookingDto.getEnd());
        Assertions.assertEquals(actualBooking.getItem().getId(), bookingDto.getItem().getId());
        Assertions.assertEquals(actualBooking.getBooker().getId(), bookingDto.getBooker().getId());
    }

    @Test
    void addBookingWithNotFoundBooker() {
        long bookerId = 10L;

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookerId, incomingBookingDto));
        Assertions.assertEquals("Пользователь с id = " + bookerId + " не найден", e.getMessage());
    }

    @Test
    void addBookingWithNotFoundItem() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(userDto2.getId(), incomingBookingDto));
        Assertions.assertEquals("Предмет с id = " + incomingBookingDto.getItemId() +
                " у пользоватея не найден", e.getMessage());
    }

    @Test
    void addBookingWithOwnerAndBookerIsSame() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(userDto.getId(), incomingBookingDto));
        Assertions.assertEquals("Нельзя забронировать свою вещь", e.getMessage());
    }

    @Test
    void addBookingWithStartIsAfterEnd() {
        incomingBookingDto.setStart(LocalDateTime.of(2022, 12, 14, 12, 12, 12));
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.addBooking(userDto2.getId(), incomingBookingDto));
        Assertions.assertEquals("Ошибка со временем аренды: время конца не должно быть раньше времени начала",
                e.getMessage());
    }

    @Test
    void addBookingWithStartAndEndIsSame() {
        incomingBookingDto.setStart(LocalDateTime.of(2022, 12, 13, 12, 12, 12));
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.addBooking(userDto2.getId(), incomingBookingDto));
        Assertions.assertEquals("Время старта и конца оренды должно различаться", e.getMessage());
    }

    @Test
    void addBookingWithAvailableIsFalse() {
        itemDto.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(itemRepository.getItemById(anyLong())).thenReturn(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto));

        AvailableException e = Assertions.assertThrows(AvailableException.class,
                () -> bookingService.addBooking(userDto2.getId(), incomingBookingDto));
        Assertions.assertEquals("Предмет с id = " + incomingBookingDto.getItemId() +
                " недоступен для бронирования", e.getMessage());
    }

    @Test
    void changeApproveStatusWithNotFoundOwner() {
        long ownerId = 10L;

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.changeApproveStatus(ownerId, bookingDto.getId(), true));
        Assertions.assertEquals("Арендодатель с id = " + ownerId + " не найден", e.getMessage());
    }

    @Test
    void changeApproveStatusWithNotFoundBooking() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.changeApproveStatus(userDto.getId(), 1, true));
        Assertions.assertEquals("Запрос с id = " + 1 + " не найден", e.getMessage());
    }

    @Test
    void changeApproveStatusWithStatusIsApproved() {
        bookingDto.setStatus(Status.APPROVED);
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(booking);

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.changeApproveStatus(userDto.getId(), booking.getId(), true));
        Assertions.assertEquals("Статус бронирования уже подтвержден", e.getMessage());
    }

    @Test
    void changeApproveStatusWithUserDontHaveItem() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(booking);

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.changeApproveStatus(userDto.getId(), booking.getId(), true));
        Assertions.assertEquals("У пользователя с id = " + userDto2.getId() + " не найден такой предмет",
                e.getMessage());
    }

    @Test
    void changeApproveStatusWithApprovedIsTrue() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto actualBookingDto = bookingService.changeApproveStatus(userDto.getId(), bookingDto.getId(), true);

        Assertions.assertEquals(actualBookingDto.getStatus(), Status.APPROVED);
    }

    @Test
    void changeApproveStatusWithApprovedIsFalse() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findById(anyLong()))
                .thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto actualBookingDto = bookingService.changeApproveStatus(userDto.getId(), bookingDto.getId(), false);

        Assertions.assertEquals(actualBookingDto.getStatus(), Status.REJECTED);
    }

    @Test
    void getBookingByWithNotFoundId() {
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userDto.getId(), 1));
        Assertions.assertEquals("Запрос с id = " + 1 + " не найден", e.getMessage());
    }

    @Test
    void getBookingByIdWithUserNotFound() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(booking);

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(1, booking.getId()));

        Assertions.assertEquals("Пользователь с id = " + 1 + " не найден", e.getMessage());
    }

    @Test
    void getBookingByIdWhenUserIsNotOwnerAndNotBooker() {
        UserDto userDto3 = UserDto.builder()
                .id(3L)
                .email("Email3@ya.ru")
                .name("User3")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto3));

        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userDto3.getId(), booking.getId()));

        Assertions.assertEquals("Пользователь не является арендодателем или арендатором", e.getMessage());
    }

    @Test
    void getBookingByIdTest() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();

        when(bookingRepository.findById(anyLong())).thenReturn(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));

        BookingDto actualBookingDto = bookingService.getBookingById(userDto.getId(), bookingDto.getId());

        Assertions.assertEquals(booking.getId(), actualBookingDto.getId());
        Assertions.assertEquals(booking.getStart(), actualBookingDto.getStart());
        Assertions.assertEquals(booking.getEnd(), actualBookingDto.getEnd());
        Assertions.assertEquals(booking.getItem().getId(), actualBookingDto.getItem().getId());
        Assertions.assertEquals(booking.getBooker().getId(), actualBookingDto.getBooker().getId());
        Assertions.assertEquals(booking.getStatus(), actualBookingDto.getStatus());
    }

    @Test
    void getBookingByNotFoundBooker() {
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByBooker(1, State.ALL, null, null));

        Assertions.assertEquals("Пользователь с id = " + 1 + " не найден", e.getMessage());
    }

    @Test
    void getBookingByBookerWithStateIsAll() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong())).thenReturn(List.of(booking));

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.ALL,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsCurrent() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerCurrent(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.CURRENT,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsPast() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.PAST,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsFuture() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.FUTURE,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStatusIsWaiting() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.WAITING,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStatusIsRejected() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.REJECTED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.REJECTED,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithInvalidPagination() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingByBooker(userDto2.getId(), State.ALL, -1, 1));

        Assertions.assertEquals("Ошибка в значениях пагинации: from < 0 или size <= 0", e.getMessage());
    }

    @Test
    void getBookingByBookerWithStateIsAllWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.ALL,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsCurrentWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerCurrent(anyLong(), any(LocalDateTime.class), any())).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.CURRENT,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsPastWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.PAST,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStateIsFutureWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.FUTURE,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStatusIsWaitingWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.WAITING,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByBookerWithStatusIsRejectedWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.REJECTED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByBooker(userDto2.getId(), State.REJECTED,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByNotFoundOwner() {
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByOwner(1, State.ALL, null, null));

        Assertions.assertEquals("Пользователь с id = " + 1 + " не найден", e.getMessage());
    }

    @Test
    void getBookingByOwnerWithInvalidPagination() {
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getBookingByOwner(userDto.getId(), State.ALL, -1, 1));

        Assertions.assertEquals("Ошибка в значениях пагинации: from < 0 или size <= 0", e.getMessage());
    }

    @Test
    void getBookingByOwnerWithStateIsAll() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findAllBookingByOwner(anyLong())).thenReturn(List.of(booking));

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.ALL,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsCurrent() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findCurrentBookingByOwner(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.CURRENT,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsPast() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findPastBookingByOwner(anyLong(), any(LocalDateTime.class))).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.PAST,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsFuture() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findFutureBookingByOwner(anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.FUTURE,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStatusIsWaiting() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findBookingByOwnerIdAndStatus(anyLong(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.WAITING,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStatusIsRejected() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.REJECTED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findBookingByOwnerIdAndStatus(anyLong(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.REJECTED,
                null, null);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsAllWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findAllBookingByOwner(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.ALL,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsCurrentWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findCurrentBookingByOwner(anyLong(), any(LocalDateTime.class), any())).thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.CURRENT,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsPastWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findPastBookingByOwner(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.PAST,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStateIsFutureWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.APPROVED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findFutureBookingByOwner(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.FUTURE,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStatusIsWaitingWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findBookingByOwnerIdAndStatus(anyLong(), any(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.WAITING,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }

    @Test
    void getBookingByOwnerWithStatusIsRejectedWithPagination() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.REJECTED)
                .build();
        List<Booking> bookings = Collections.singletonList(booking);
        when(userRepository.findById(anyLong())).thenReturn(UserMapper.toUser(userDto));
        when(bookingRepository.findBookingByOwnerIdAndStatus(anyLong(), any(), any()))
                .thenReturn(bookings);

        List<BookingDto> actualBookingDtoList = bookingService.getBookingByOwner(userDto.getId(), State.REJECTED,
                0, 1);
        Assertions.assertEquals(actualBookingDtoList.get(0).getId(), booking.getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStart(), booking.getStart());
        Assertions.assertEquals(actualBookingDtoList.get(0).getEnd(), booking.getEnd());
        Assertions.assertEquals(actualBookingDtoList.get(0).getBooker().getId(), booking.getBooker().getId());
        Assertions.assertEquals(actualBookingDtoList.get(0).getStatus(), booking.getStatus());
    }
}
