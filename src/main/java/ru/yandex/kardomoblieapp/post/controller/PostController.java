package ru.yandex.kardomoblieapp.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.post.dto.CommentDto;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.dto.PostDto;
import ru.yandex.kardomoblieapp.post.mapper.CommentMapper;
import ru.yandex.kardomoblieapp.post.mapper.PostMapper;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostSort;
import ru.yandex.kardomoblieapp.post.service.PostService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/posts")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Посты пользователей", description = "Взаимодействие с постами пользователей")
public class PostController {

    private final PostService postService;

    private final PostMapper postMapper;

    private final CommentMapper commentMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Публикация нового поста")
    public PostDto createPost(@RequestParam("file") @Parameter(description = "Файл") MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam("content") @Parameter(description = "Имя поста") String content,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' публикует новый пост.", principal.getName());
        final Post createdPost = postService.createPost(principal.getName(), file, content);
        return postMapper.toDto(createdPost);
    }

    @PatchMapping("/{postId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Редактирование поста")
    public PostDto updatePost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @RequestParam(value = "files", required = false)
                              @Parameter(description = "Файл") MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam(value = "content", required = false)
                              @Parameter(description = "Имя поста") String content,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' обновляет пост с id '{}'.", principal.getName(), postId);
        final Post updatedPost = postService.updatePost(principal.getName(), postId, file, content);
        return postMapper.toDto(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление поста")
    public void deletePost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                           @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' удаляет пост с id '{}'.", principal.getName(), postId);
        postService.deletePost(principal.getName(), postId);
    }

    @GetMapping("/{postId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск поста по идентификатору")
    public PostDto getPostById(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                               @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' запрашивает пост с id '{}'.", principal.getName(), postId);
        final Post post = postService.findPostById(postId);
        return postMapper.toDto(post);
    }

    @GetMapping
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск всех постов пользователя")
    public List<PostDto> getAllPostByUser(@RequestParam @Parameter(description = "Идентификатор поста") long userId) {
        log.info("Получение всех постов пользователя.");
        List<Post> userPosts = postService.findPostsFromUser(userId);
        return postMapper.toDtoList(userPosts);
    }

    @PutMapping("/{postId}/like")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Добавление лайка посту")
    public long addLikeToPost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' ставит лайк посту с id '{}'.", principal.getName(), postId);
        return postService.addLikeToPost(principal.getName(), postId);
    }

    @GetMapping("/feed")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение ленты постов")
    public List<PostDto> getPostsFeed(@RequestParam(defaultValue = "0")
                                      @Parameter(description = "Номер страницы") Integer page,
                                      @RequestParam(defaultValue = "10")
                                      @Parameter(description = "Количество постов на странице") Integer size) {
        log.info("Получение ленты постов. from = '{}', size = '{}'.", page, size);
        List<Post> feed = postService.getPostsFeed(page, size);
        return postMapper.toDtoList(feed);
    }

    @GetMapping("/recommendations")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение рекомендаций")
    public List<PostDto> getRecommendations(@RequestParam(defaultValue = "0")
                                            @Parameter(description = "Номер страницы") Integer page,
                                            @RequestParam(defaultValue = "10")
                                            @Parameter(description = "Количество постов на странице") Integer size,
                                            @RequestParam(defaultValue = "LIKES")
                                            @Parameter(description = "Параметр сортировки") PostSort sort,
                                            @Parameter(hidden = true) Principal principal) {
        log.info("Получение рекомендаций. from: '{}, size: '{}', sort: '{}'.", page, size, sort);
        List<Post> recommendations = postService.getRecommendations(principal.getName(), page, size, sort);
        return postMapper.toDtoList(recommendations);
    }

    @PostMapping("/{postId}/comment")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Добавление комментария к посту")
    public CommentDto addCommentToPost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                                       @RequestBody @Valid @Parameter(description = "Новый комментарий") CommentRequest commentRequest,
                                       @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' добавляет комментарий к посту с id '{}'.", principal.getName(), postId);
        Comment newComment = commentMapper.toModel(commentRequest);
        Comment comment = postService.addCommentToPost(principal.getName(), postId, newComment);
        return commentMapper.toDto(comment);
    }

    @PatchMapping("/{postId}/comment/{commentId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Редактирование комментария")
    public CommentDto updateComment(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                                    @PathVariable @Parameter(description = "Идентификатор комментария") long commentId,
                                    @RequestBody @Valid @Parameter(description = "Новый комментарий") CommentRequest commentRequest,
                                    @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' добавляет комментарий c id '{}'.", principal.getName(), commentId);
        Comment updatedComment = postService.updateComment(principal.getName(), commentId, commentRequest);
        return commentMapper.toDto(updatedComment);
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление комментария")
    public void deleteComment(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @PathVariable @Parameter(description = "Идентификатор комментария") long commentId,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' удаляет комментарий c id '{}'.", principal.getName(), commentId);
        postService.deleteComment(principal.getName(), commentId);
    }
}
