package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.IncomingItemRequestDto;
import ru.practicum.shareit.request.dto.OutItemRequestDto;
import ru.practicum.shareit.request.dto.OutLongItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    UserDto userDto;

    UserDto userDto2;

    OutItemRequestDto outRequestDto;

    IncomingItemRequestDto itemRequestDto;

    ItemDto itemDto;

    OutLongItemRequestDto longItemRequestDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .email("Email@ya.ru")
                .name("User")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .email("Email2@ya.ru")
                .name("User2")
                .build();
        outRequestDto = OutItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2023, 10, 10, 12, 10, 15))
                .build();
        itemRequestDto = IncomingItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();
        longItemRequestDto = OutLongItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .created(LocalDateTime.of(2023, 10, 10, 12, 10, 15))
                .items(List.of(itemDto))
                .build();
    }

    @Test
    void addRequestTest() throws Exception {
        when(itemRequestService.addItemRequest(any(), anyLong())).thenReturn(outRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value(outRequestDto.getDescription()))
                .andExpect(jsonPath("$.created").value(outRequestDto.getCreated().toString()));
    }

    @Test
    void addRequestWithDescriptionIsEmptyTest() throws Exception {
        itemRequestDto.setDescription("");
        when(itemRequestService.addItemRequest(any(), anyLong())).thenReturn(outRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequestByIdTest() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenReturn(longItemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value(longItemRequestDto.getDescription()))
                .andExpect(jsonPath("$.created").value(longItemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$.items").isNotEmpty());

    }

    @Test
    void getRequestorItemRequestTest() throws Exception {
        when(itemRequestService.getRequestorItemRequest(anyLong())).thenReturn(List.of(longItemRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].description").value(longItemRequestDto.getDescription()))
                .andExpect(jsonPath("$.[0].created").value(longItemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$.[0].items").isNotEmpty());
    }

    @Test
    void getAllOtherItemRequestsTest() throws Exception {
        when(itemRequestService.getAllOtherItemRequest(anyLong(), anyInt(), anyInt())).thenReturn(List.of(longItemRequestDto));

        mockMvc.perform(get("/requests/all?from=0&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").exists())
                .andExpect(jsonPath("$.[0].description").value(longItemRequestDto.getDescription()))
                .andExpect(jsonPath("$.[0].created").value(longItemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$.[0].items").isNotEmpty());
    }
}
