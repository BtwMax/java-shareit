package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.IncomingBookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    BookingDto bookingDto;

    UserDto userDto;

    UserDto userDto2;

    ItemDto itemDto;

    IncomingBookingDto incomingBookingDto;

    @BeforeEach
    void createItemAndBookingAndUser() {

        userDto = UserDto.builder()
                .id(1L)
                .email("Email@ya.ru")
                .name("User")
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        userDto2 = UserDto.builder()
                .id(2L)
                .email("Email2@ya.ru")
                .name("User2")
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 10, 10, 10, 10, 30))
                .end(LocalDateTime.of(2023, 10, 11, 10, 10, 30))
                .item(itemDto)
                .booker(UserMapper.toShortUserDto(UserMapper.toUser(userDto)))
                .status(Status.WAITING)
                .build();

        incomingBookingDto = IncomingBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2023, 10, 10, 10, 10, 30))
                .end(LocalDateTime.of(2023, 10, 11, 10, 10, 30))
                .build();
    }

    @Test
    void addBookingTest() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(incomingBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.item").isNotEmpty())
                .andExpect(jsonPath("$.booker").isNotEmpty())
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void shouldReturnBookingWithChangedStatus() throws Exception {
        bookingDto.setStatus(Status.APPROVED);
        when(bookingService.changeApproveStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.item").isNotEmpty())
                .andExpect(jsonPath("$.booker").isNotEmpty())
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.item").isNotEmpty())
                .andExpect(jsonPath("$.booker").isNotEmpty())
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void shouldReturnListOfBookingsOfBooker() throws Exception {
        when(bookingService.getBookingByBooker(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings?state=all&from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.[0].start").value(bookingDto.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(bookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.[0].item").isNotEmpty())
                .andExpect(jsonPath("$.[0].booker").isNotEmpty())
                .andExpect(jsonPath("$.[0].status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void shouldReturnListOfBookingsOfItemOwner() throws Exception {
        when(bookingService.getBookingByOwner(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(get("/bookings/owner?state=all&from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.[0].start").value(bookingDto.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(bookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.[0].item").isNotEmpty())
                .andExpect(jsonPath("$.[0].booker").isNotEmpty())
                .andExpect(jsonPath("$.[0].status").value(bookingDto.getStatus().toString()));
    }

    @Test
    void shouldReturnExceptionOfBookingsOfBooker() throws Exception {
        mockMvc.perform(get("/bookings?state=unsupported&from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
