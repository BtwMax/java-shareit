package ru.practicum.gateway.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class ItemDto {

    private long id;
    @Size(max = 255, message = "Длина имени должна быть не более 255 символов")
    private String name;
    @Size(max = 510, message = "Длина описания должна быть не более 510 символов")
    private String description;
    private Boolean available;
    private Long requestId;
}
