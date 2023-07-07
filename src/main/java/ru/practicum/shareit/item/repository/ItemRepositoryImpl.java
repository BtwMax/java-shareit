package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    @Override
    public Item addItem(Item item) {
        validate(item);
        if (item.getId() == 0) {
            item.setId(generatorId());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItemById(long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Предмет с id = " + id + " не найден");
        }
        return items.get(id);
    }

    @Override
    public Item updateItem(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Невозможно обновить несуществующий предмет");
        }
        Item oldItem = getItemById(item.getId());
        long ownerId = oldItem.getOwner();
        if (ownerId != item.getOwner()) {
            throw new NotFoundException("У пользователя с id = " + item.getOwner() + " нет такого предмета");
        }
        if (item.getName() == null) {
            item.setName(oldItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(oldItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(oldItem.getAvailable());
        }
        validate(item);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> getUserItems(long id) {
        return items.values().stream()
                .filter(item -> item.getOwner() == id)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Item> findItemsByText(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.getAvailable())
                .collect(Collectors.toList());
    }

    private void validate(Item item) {
        if (item.getName() == null || item.getName().isBlank() || item.getName().isEmpty()) {
            throw new ValidationException("Ошибка валидации названия");
        }
        if (item.getDescription() == null || item.getDescription().isBlank() || item.getDescription().isEmpty()) {
            throw new ValidationException("Ошибка валидации описания");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Ошибка валидации статуса доступности");
        }
    }

    private long generatorId() {
        return id++;
    }
}
