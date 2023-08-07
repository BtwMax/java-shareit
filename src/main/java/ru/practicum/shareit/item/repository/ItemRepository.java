package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item getItemById(long id);

    Item findById(long id);

    Collection<Item> findAllByOwnerIdOrderById(long id);

    List<Item> findItemsByNameOrDescriptionContainingIgnoreCase(String name, String description);

    List<Item> findItemsByItemRequestId(long itemRequestId);
}
