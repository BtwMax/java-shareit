package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItem;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public ItemDto addItem(long userId, ItemDto itemDto) {
        userRepository.getUserById(userId);
        Item item = toItem(itemDto.getId(), userId, itemDto);
        Item itemStorage = itemRepository.addItem(item);
        return toItemDto(itemStorage.getId(), itemStorage);
    }

    public ItemDto getItemById(long id) {
        Item item = itemRepository.getItemById(id);
        return toItemDto(item.getId(), item);
    }

    public ItemDto updateItem(long id, long userId, ItemDto itemDto) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Нельзя добавить предмет к несуществующему пользователю");
        }
        Item item = toItem(id, userId, itemDto);
        Item itemStorage = itemRepository.updateItem(item);
        return toItemDto(itemStorage.getId(), itemStorage);
    }

    public Collection<ItemDto> getUserItems(long id) {
        return itemRepository.getUserItems(id).stream()
                .map(item -> toItemDto(item.getId(), item))
                .collect(Collectors.toList());
    }

    public Collection<ItemDto> findItemsByText(String text) {
        return itemRepository.findItemsByText(text).stream()
                .map(item -> toItemDto(item.getId(), item))
                .collect(Collectors.toList());
    }
}
