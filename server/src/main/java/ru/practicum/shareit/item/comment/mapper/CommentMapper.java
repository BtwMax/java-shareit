package ru.practicum.shareit.item.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.comment.dto.IncomingCommentDto;
import ru.practicum.shareit.item.comment.dto.OutCommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public Comment toComment(IncomingCommentDto incomingCommentDto, Item item, User user, LocalDateTime dateTime) {
        return Comment.builder()
                .id(incomingCommentDto.getId())
                .text(incomingCommentDto.getText())
                .item(item)
                .author(user)
                .created(dateTime)
                .build();
    }

    public OutCommentDto toOutCommentDto(Comment comment) {
        return OutCommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
