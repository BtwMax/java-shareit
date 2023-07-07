package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static Item toItem(long id, long userId, ItemDto itemDto) {
        return Item.builder()
                .id(id)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(userId)
                .build();
    }

    public static ItemDto toItemDto(long id, Item item) {
        return ItemDto.builder()
                .id(id)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}
