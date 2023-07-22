package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item getItemById(long id);

    Collection<Item> findAllByOwnerIdOrderById(long id);

    Collection<Item> findItemsByNameOrDescriptionContainingIgnoreCase(String name, String description);
}
