package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.IncomingBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public Booking toBooking(IncomingBookingDto incomingBookingDto, Item item, User user, Status status) {
        return Booking.builder()
                .start(incomingBookingDto.getStart())
                .end(incomingBookingDto.getEnd())
                .item(item)
                .booker(user)
                .status(status)
                .build();
    }

    public BookingDto toBookingDto(Booking booking, ItemDto itemDto, ShortUserDto userDto) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(userDto)
                .status(booking.getStatus())
                .build();

    }

    public BookingForItemDto toBookingForItemDto(Booking booking) {
        return BookingForItemDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
