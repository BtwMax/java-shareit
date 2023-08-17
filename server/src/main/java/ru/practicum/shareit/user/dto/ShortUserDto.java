package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class ShortUserDto {

    private long id;
    private String name;
}
