package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.IncomingBookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ServerErrorException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                 @RequestBody IncomingBookingDto incomingBookingDto) {
        log.info("Добавление запроса на аренду предмета");
        return bookingService.addBooking(bookerId, incomingBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") long ownerId, @PathVariable long bookingId,
                                     @RequestParam boolean approved) {
        log.info("Изменения статуса запроса хозяином предмета");
        return bookingService.changeApproveStatus(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") long id, @PathVariable long bookingId) {
        log.info("Запрос на вывод бронирования с id = " + bookingId);
        return bookingService.getBookingById(id, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingForBooker(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                @RequestParam(defaultValue = "ALL") String state,
                                                @RequestParam(required = false) Integer from,
                                                @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод бронирований для арендатора");
        State bookingState = State.from(state)
                .orElseThrow(() -> new ServerErrorException("Unknown state: " + state));
        return bookingService.getBookingByBooker(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingForOwner(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                               @RequestParam(defaultValue = "ALL") String state,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        log.info("Запрос на вывод бронирований для арендодателя");
        State bookingState = State.from(state)
                .orElseThrow(() -> new ServerErrorException("Unknown state: " + state));
        return bookingService.getBookingByOwner(ownerId, bookingState, from, size);
    }
}
