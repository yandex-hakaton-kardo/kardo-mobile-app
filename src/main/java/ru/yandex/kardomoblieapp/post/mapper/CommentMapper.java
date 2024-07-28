package ru.yandex.kardomoblieapp.post.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.kardomoblieapp.post.dto.CommentDto;
import ru.yandex.kardomoblieapp.post.dto.NewCommentRequest;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(source = "author.id", target = "authorId")
    CommentDto toDto(Comment comment);

    Comment toModel(NewCommentRequest newCommentRequest);
}
