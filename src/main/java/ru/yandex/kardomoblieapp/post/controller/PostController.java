package ru.yandex.kardomoblieapp.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import ru.yandex.kardomoblieapp.post.dto.PostSearchFilter;
import ru.yandex.kardomoblieapp.post.dto.PostWithLikeDto;
import ru.yandex.kardomoblieapp.post.mapper.CommentMapper;
import ru.yandex.kardomoblieapp.post.mapper.PostMapper;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostSort;
import ru.yandex.kardomoblieapp.post.model.PostWithLike;
import ru.yandex.kardomoblieapp.post.service.PostService;
import ru.yandex.kardomoblieapp.shared.exception.ErrorResponse;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Публикация нового поста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пост успешно создан", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))}),
            @ApiResponse(responseCode = "400", description = "Некорректное название поста", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Отсутствует название поста или не прикреплен файл", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при сохранении файла", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public PostDto createPost(@Parameter(description = "Файл")
                              @RequestParam("file") MultipartFile file,
                              @Size(min = 2, max = 100, message = "Текст должен содержать от 2 до 100 символов")
                              @NotBlank(message = "Текст должен содержать от 2 до 100 символов")
                              @RequestParam("content") @Parameter(description = "Название поста") String content,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' публикует новый пост.", principal.getName());
        final Post createdPost = postService.createPost(principal.getName(), file, content);
        return postMapper.toDto(createdPost);
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Редактирование поста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пост успешно обновлен", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))}),
            @ApiResponse(responseCode = "400", description = "Некорректное название поста", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пост не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при сохранении файла", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public PostDto updatePost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @RequestParam(value = "files", required = false)
                              @Parameter(description = "Файл") MultipartFile file,
                              @Size(min = 2, max = 100, message = "Название поста должно содержать от 2 до 100 символов")
                              @RequestParam(value = "content", required = false)
                              @Parameter(description = "Содержание поста") String content,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' обновляет пост с id '{}'.", principal.getName(), postId);
        final Post updatedPost = postService.updatePost(principal.getName(), postId, file, content);
        return postMapper.toDto(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление поста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пост успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пост не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при удалении файла", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void deletePost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                           @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' удаляет пост с id '{}'.", principal.getName(), postId);
        postService.deletePost(postId, principal.getName());
    }

    @GetMapping("/{postId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск поста по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пост найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostWithLikeDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пост не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при получении файла", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public PostWithLikeDto getPostById(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                                       @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь c id '{}' запрашивает пост с id '{}'.", principal.getName(), postId);
        final PostWithLike post = postService.findPostById(postId, principal.getName());
        return postMapper.toDto(post);
    }

    @GetMapping
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск всех постов пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список постов пользователя", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<PostDto> getAllPostByUser(@RequestParam @Parameter(description = "Идентификатор поста") long userId) {
        log.info("Получение всех постов пользователя.");
        List<Post> userPosts = postService.findPostsFromUser(userId);
        return postMapper.toDtoList(userPosts);
    }

    @PutMapping("/{postId}/like")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Добавление лайка посту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лайк успешно добавлен", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пост не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public long addLikeToPost(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' ставит лайк посту с id '{}'.", principal.getName(), postId);
        return postService.addLikeToPost(principal.getName(), postId);
    }

    @GetMapping("/feed")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение ленты постов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лента получена", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список рекомендаций получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<PostDto> getRecommendations(@RequestParam(defaultValue = "0")
                                            @Parameter(description = "Номер страницы") Integer page,
                                            @RequestParam(defaultValue = "10")
                                            @Parameter(description = "Количество постов на странице") Integer size,
                                            @RequestParam(defaultValue = "LIKES")
                                            @Parameter(description = "Тип сортировки") PostSort sort,
                                            @Parameter(hidden = true) Principal principal) {
        log.info("Получение рекомендаций. from: '{}, size: '{}', sort: '{}'.", page, size, sort);
        List<Post> recommendations = postService.getRecommendations(principal.getName(), page, size, sort);
        return postMapper.toDtoList(recommendations);
    }

    @PostMapping("/{postId}/comment")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Добавление комментария к посту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно добавлен", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пост не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно обновлен", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))}),
            @ApiResponse(responseCode = "400", description = "Некорректное название поста", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет прав на редактирование комментария",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий успешно удален", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет прав на редактирование комментария",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void deleteComment(@PathVariable @Parameter(description = "Идентификатор поста") long postId,
                              @PathVariable @Parameter(description = "Идентификатор комментария") long commentId,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' удаляет комментарий c id '{}'.", principal.getName(), commentId);
        postService.deleteComment(principal.getName(), commentId);
    }

    @GetMapping("/search")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск постов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список постов получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<PostDto> searchPosts(@Parameter(description = "Фильтр поиска") PostSearchFilter searchFilter,
                                     @RequestParam(defaultValue = "0")
                                     @Parameter(description = "Номер страницы") Integer page,
                                     @RequestParam(defaultValue = "10")
                                     @Parameter(description = "Количество постов на странице") Integer size) {
        log.info("Поиск постов по фильтру");
        final List<Post> posts = postService.searchPosts(searchFilter, page, size);
        return postMapper.toDtoList(posts);
    }
}
