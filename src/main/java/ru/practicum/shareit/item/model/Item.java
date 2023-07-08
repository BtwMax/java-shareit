package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@Builder
public class Item {

    private long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
}
