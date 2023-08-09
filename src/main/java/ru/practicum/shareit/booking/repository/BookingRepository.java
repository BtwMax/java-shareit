package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Booking findById(long id);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    /*Запросы для арендатора с пагинацией*/
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerCurrent(long id, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long id, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long id, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status, Pageable pageable);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId, Pageable pageable);

    /*Запросы для арендатора без пагинации*/
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerCurrent(long id, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long id, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long id, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status);


    /*Запросы для арендодателя с пагинацией*/
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingByOwner(long id, Pageable pageable);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingByOwner(long id, LocalDateTime dateTime, Pageable pageable);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 < b.start " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureBookingByOwner(long id, LocalDateTime dateTime, Pageable pageable);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 > b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findPastBookingByOwner(long id, LocalDateTime dateTime, Pageable pageable);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND b.status = ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findBookingByOwnerIdAndStatus(long id, Status status, Pageable pageable);

    /*Запросы для арендодателя без пагинации*/
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 " +
            "ORDER BY b.start DESC")
    List<Booking> findAllBookingByOwner(long id);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingByOwner(long id, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 < b.start " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureBookingByOwner(long id, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND ?2 > b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findPastBookingByOwner(long id, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = ?1 AND b.status = ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> findBookingByOwnerIdAndStatus(long id, Status status);


    @Query(value = "SELECT b.* FROM booking AS b " +
            "WHERE b.item_id = ?1 " +
            "AND ?2 > b.start_time AND b.status = 'APPROVED' " +
            "ORDER BY b.start_time DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Booking> findItemLastBooking(long id, LocalDateTime dateTime);

    @Query(value = "SELECT b.* " +
            "FROM booking AS b " +
            "WHERE b.item_id = ?1 AND ?2 < b.start_time AND b.status = 'APPROVED' " +
            "ORDER BY b.start_time " +
            "LIMIT 1", nativeQuery = true)
    Optional<Booking> findItemNextBooking(long id, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.id = ?1 AND b.booker.id = ?2 " +
            "AND ?3 > b.end")
    List<Booking> findFinishedBookingsByItem(long itemId, long bookerId, LocalDateTime dateTime);
}
