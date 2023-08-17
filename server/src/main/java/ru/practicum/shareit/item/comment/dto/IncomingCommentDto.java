package ru.practicum.shareit.item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class IncomingCommentDto {

    private long id;

    private String text;
}
