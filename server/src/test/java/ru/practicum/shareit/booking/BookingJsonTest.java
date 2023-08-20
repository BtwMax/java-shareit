package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;

@JsonTest
public class BookingJsonTest {

    Booking booking;

    ItemDto itemDto;

    UserDto userDto;

    UserDto userDto2;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Item description")
                .available(true)
                .build();
        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@ya.ru")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .name("User2")
                .email("user2@ya.ru")
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .item(ItemMapper.toItem(UserMapper.toUser(userDto), itemDto))
                .booker(UserMapper.toUser(userDto2))
                .status(Status.WAITING)
                .build();
    }

    @Test
    void bookingForItemDto() {
        BookingForItemDto bookingForItemDto = null;
        bookingForItemDto = BookingMapper.toBookingForItemDto(booking);
        Assertions.assertEquals(bookingForItemDto.getId(), booking.getId());
        Assertions.assertEquals(bookingForItemDto.getBookerId(), booking.getBooker().getId());
    }
}
