package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    ItemRequest findById(long id);

    List<ItemRequest> findItemRequestByRequestorIdOrderByCreatedDesc(long requestorId);

    @Query("SELECT ir FROM ItemRequest AS ir " +
            "WHERE ir.requestor.id <> ?1 " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllOtherItemRequest(long requestorId);

    @Query("SELECT ir FROM ItemRequest AS ir " +
            "WHERE ir.requestor.id <> ?1 " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllOtherItemRequest(long requestorId, Pageable pageable);
}
