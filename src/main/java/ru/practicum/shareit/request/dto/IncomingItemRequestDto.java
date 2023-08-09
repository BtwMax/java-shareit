package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class IncomingItemRequestDto {

    private long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}
