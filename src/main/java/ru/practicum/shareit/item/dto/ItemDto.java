package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO Sprint add-controllers.
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ItemDto {

    private long id;
    private String name;
    private String description;
    private Boolean available;
}
