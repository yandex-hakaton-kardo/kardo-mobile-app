package ru.yandex.kardomoblieapp.post.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.datafiles.mapper.DataFileMapper;
import ru.yandex.kardomoblieapp.post.dto.PostDto;
import ru.yandex.kardomoblieapp.post.dto.PostWithLikeDto;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostWithLike;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DataFileMapper.class})
public interface PostMapper {

    PostDto toDto(Post post);

    List<PostDto> toDtoList(List<Post> userPosts);

    PostWithLikeDto toDto(PostWithLike post);
}
