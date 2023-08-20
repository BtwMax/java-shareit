package ru.practicum.gateway.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.gateway.exceptions.ServerErrorException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingClient bookingClient;

    @Autowired
    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                             @Valid @RequestBody IncomingBookingDto incomingBookingDto) {
        log.info("Добавление запроса на аренду предмета");
        return bookingClient.addBooking(bookerId, incomingBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                 @PathVariable long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Изменения статуса запроса хозяином предмета");
        return bookingClient.changeApproveStatus(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") long id,
                                                 @PathVariable long bookingId) {
        log.info("Запрос на вывод бронирования с id = " + bookingId);
        return bookingClient.getBookingById(id, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingForBooker(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @PositiveOrZero @RequestParam(required = false) Integer from,
                                                      @Positive @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод бронирований для арендатора");
        State bookingState = State.from(state)
                .orElseThrow(() -> new ServerErrorException("Unknown state: " + state));
        return bookingClient.getBookingsByBooker(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingForOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @PositiveOrZero @RequestParam(required = false) Integer from,
                                                     @Positive @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод бронирований для арендодателя");
        State bookingState = State.from(state)
                .orElseThrow(() -> new ServerErrorException("Unknown state: " + state));
        return bookingClient.getBookingsForOwner(ownerId, bookingState, from, size);
    }
}
