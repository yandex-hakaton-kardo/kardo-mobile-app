package ru.yandex.kardomoblieapp.post.controller;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.post.dto.PostDto;
import ru.yandex.kardomoblieapp.post.mapper.PostMapper;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    private final PostMapper postMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto createPost(@RequestHeader("X-Kardo-User-Id") long requesterId,
                              @RequestParam(value = "files") MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam("content") String content) {
        log.info("Пользователь c id '{}' публикует новый пост.", requesterId);
        final Post createdPost = postService.createPost(requesterId, file, content);
        return postMapper.toDto(createdPost);
    }

    @PatchMapping("/{postId}")
    public PostDto updatePost(@RequestHeader("X-Kardo-User-Id") long requesterId,
                              @PathVariable long postId,
                              @RequestParam(value = "files", required = false) MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam(value = "content", required = false) String content) {
        log.info("Пользователь c id '{}' обновляет пост с id '{}'.", requesterId, postId);
        final Post updatedPost = postService.updatePost(requesterId, postId, file, content);
        return postMapper.toDto(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@RequestHeader("X-Kardo-User-Id") long requesterId,
                           @PathVariable long postId) {
        log.info("Пользователь c id '{}' удаляет пост с id '{}'.", requesterId, postId);
        postService.deletePost(requesterId, postId);
    }

    @GetMapping("/{postId}")
    public PostDto getPostById(@RequestHeader("X-Kardo-User-Id") long requesterId,
                               @PathVariable long postId) {
        log.info("Пользователь c id '{}' запрашивает пост с id '{}'.", requesterId, postId);
        final Post post = postService.findPostById(postId);
        return postMapper.toDto(post);
    }

    @GetMapping
    public List<PostDto> getAllPostByUser(@RequestHeader("X-Kardo-User-Id") long requesterId,
                                          @RequestParam long userId) {
        log.info("Получение всех постов пользователя.");
        List<Post> userPosts = postService.findPostsFromUser(userId);
        return postMapper.toDtoList(userPosts);
    }
}
