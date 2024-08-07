package ru.yandex.kardomoblieapp.post.service;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.dto.PostSearchFilter;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostSort;

import java.util.List;

public interface PostService {
    Post createPost(String username, MultipartFile file, String content);

    Post updatePost(String username, long postId, MultipartFile file, String content);

    void deletePost(String username, long postId);

    Post findPostById(long postId);

    List<Post> findPostsFromUser(long userId);

    long addLikeToPost(String username, long postId);

    List<Post> getPostsFeed(Integer from, Integer size);

    Comment addCommentToPost(String username, long postId, Comment newComment);

    Comment updateComment(String username, long commentId, CommentRequest commentRequest);

    void deleteComment(String username, long commentId);

    List<Post> getRecommendations(String username, Integer from, Integer size, PostSort sort);

    List<Post> searchPosts(PostSearchFilter searchFilter, Integer page, Integer size);
}
