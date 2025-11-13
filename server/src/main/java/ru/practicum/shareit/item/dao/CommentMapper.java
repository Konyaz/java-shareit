package ru.practicum.shareit.item.dao;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    // Маппинг объекта Comment в CommentDto
    @Mapping(source = "author", target = "authorName", qualifiedByName = "mapAuthorToAuthorName")
    CommentDto toCommentDto(Comment comment);

    // Маппинг автора комментария в имя автора
    @Named("mapAuthorToAuthorName")
    default String mapAuthorToAuthorName(User author) {
        if (author == null) {
            return null;
        }
        return author.getName();
    }
}