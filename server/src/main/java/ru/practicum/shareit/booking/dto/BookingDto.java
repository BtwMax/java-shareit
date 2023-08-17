package ru.practicum.shareit.booking.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.ShortUserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BookingDto {

    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto item;
    private ShortUserDto booker;
    private Status status;
}
