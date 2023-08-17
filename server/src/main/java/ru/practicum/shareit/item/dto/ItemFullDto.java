package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.comment.dto.OutCommentDto;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ItemFullDto {


    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<OutCommentDto> comments;
}
