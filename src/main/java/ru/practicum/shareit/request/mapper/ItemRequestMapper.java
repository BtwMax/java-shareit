package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.IncomingItemRequestDto;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.dto.OutLongItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequest toItemRequest(User requestor, IncomingItemRequestDto itemRequestDto, LocalDateTime dateTime) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestor(requestor)
                .created(dateTime)
                .build();
    }

    public OutItemRequestDto toOutItemRequestDto(ItemRequest itemRequest) {
        return OutItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public OutLongItemRequestDto toOutLongItemRequestDto(ItemRequest itemRequest, List<ItemDto> items) {
        return OutLongItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }
}
