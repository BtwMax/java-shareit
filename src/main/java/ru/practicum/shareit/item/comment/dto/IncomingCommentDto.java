package ru.practicum.shareit.item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class IncomingCommentDto {

    private long id;

    @NotBlank
    private String text;
}
