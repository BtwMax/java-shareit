package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    Item addItem(Item item);

    Item getItemById(long id);

    Item updateItem(Item item);

    Collection<Item> getUserItems(long id);

    Collection<Item> findItemsByText(String text);
}
