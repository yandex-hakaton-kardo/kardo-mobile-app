package ru.yandex.kardomoblieapp.post.controller;

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
    public PostDto createPost(@RequestParam("file") MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam("content") String content,
                              Principal principal) {
        log.info("Пользователь c id '{}' публикует новый пост.", principal.getName());
        final Post createdPost = postService.createPost(principal.getName(), file, content);
        return postMapper.toDto(createdPost);
    }

    @PatchMapping("/{postId}")
    public PostDto updatePost(@PathVariable long postId,
                              @RequestParam(value = "files", required = false) MultipartFile file,
                              @Size(min = 10, max = 250, message = "Текст должен содержать от 10 до 250 символов")
                              @RequestParam(value = "content", required = false) String content,
                              Principal principal) {
        log.info("Пользователь c id '{}' обновляет пост с id '{}'.", principal.getName(), postId);
        final Post updatedPost = postService.updatePost(principal.getName(), postId, file, content);
        return postMapper.toDto(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable long postId,
                           Principal principal) {
        log.info("Пользователь c id '{}' удаляет пост с id '{}'.", principal.getName(), postId);
        postService.deletePost(principal.getName(), postId);
    }

    @GetMapping("/{postId}")
    public PostDto getPostById(@PathVariable long postId,
                               Principal principal) {
        log.info("Пользователь c id '{}' запрашивает пост с id '{}'.", principal.getName(), postId);
        final Post post = postService.findPostById(postId);
        return postMapper.toDto(post);
    }

    @GetMapping
    public List<PostDto> getAllPostByUser(@RequestParam long userId) {
        log.info("Получение всех постов пользователя.");
        List<Post> userPosts = postService.findPostsFromUser(userId);
        return postMapper.toDtoList(userPosts);
    }

    @PutMapping("/{postId}/like")
    public long addLikeToPost(@PathVariable long postId,
                              Principal principal) {
        log.info("Пользователь с id '{}' ставит лайк посту с id '{}'.", principal.getName(), postId);
        return postService.addLikeToPost(principal.getName(), postId);
    }

    @GetMapping("/feed")
    public List<PostDto> getPostsFeed(@RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение ленты постов. from = '{}', size = '{}'.", page, size);
        List<Post> feed = postService.getPostsFeed(page, size);
        return postMapper.toDtoList(feed);
    }

    @GetMapping("/recommendations")
    public List<PostDto> getRecommendations(@RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(defaultValue = "LIKES") PostSort sort,
                                            Principal principal) {
        log.info("Получение рекомендаций. from: '{}, size: '{}', sort: '{}'.", page, size, sort);
        List<Post> recommendations = postService.getRecommendations(principal.getName(), page, size, sort);
        return postMapper.toDtoList(recommendations);
    }

    @PostMapping("/{postId}/comment")
    public CommentDto addCommentToPost(@PathVariable long postId,
                                       @RequestBody @Valid CommentRequest commentRequest,
                                       Principal principal) {
        log.info("Пользователь с id '{}' добавляет комментарий к посту с id '{}'.", postId);
        Comment newComment = commentMapper.toModel(commentRequest);
        Comment comment = postService.addCommentToPost(principal.getName(), postId, newComment);
        return commentMapper.toDto(comment);
    }

    @PatchMapping("/{postId}/comment/{commentId}")
    public CommentDto updateComment(@PathVariable long postId,
                                    @PathVariable long commentId,
                                    @RequestBody @Valid CommentRequest commentRequest,
                                    Principal principal) {
        log.info("Пользователь с id '{}' добавляет комментарий c id '{}'.", commentId);
        Comment updatedComment = postService.updateComment(principal.getName(), commentId, commentRequest);
        return commentMapper.toDto(updatedComment);
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public void deleteComment(@PathVariable long postId,
                              @PathVariable long commentId,
                              Principal principal) {
        log.info("Пользователь с id '{}' удаляет комментарий c id '{}'.", principal.getName(), commentId);
        postService.deleteComment(principal.getName(), commentId);
    }
}
