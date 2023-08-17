package ru.practicum.gateway.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class IncomingBookingDto {

    @NotNull
    private long itemId;

    @FutureOrPresent
    private LocalDateTime start;

    @Future
    private LocalDateTime end;
}
