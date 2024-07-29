package ru.yandex.kardomoblieapp.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostLike;
import ru.yandex.kardomoblieapp.post.model.PostLikeId;
import ru.yandex.kardomoblieapp.post.repository.CommentRepository;
import ru.yandex.kardomoblieapp.post.repository.PostLikeRepository;
import ru.yandex.kardomoblieapp.post.repository.PostRepository;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final UserService userService;

    private final DataFileService dataFileService;

    private final PostLikeRepository postLikeRepository;

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Post createPost(long requesterId, MultipartFile file, String content) {
        final User author = userService.findUserById(requesterId);
        final Post newPost = Post.builder()
                .author(author)
                .title(content)
                .build();

        DataFile uploadedFile = dataFileService.uploadFile(file, requesterId);
        newPost.setFile(uploadedFile);
        uploadedFile.setPost(newPost);
        Post savedPost = postRepository.save(newPost);
        log.info("Пользователь с id '{}' создал пост с id '{}'.", requesterId, savedPost.getId());
        return savedPost;
    }

    @Override
    @Transactional
    public Post updatePost(long requesterId, long postId, MultipartFile file, String content) {
        userService.findUserById(requesterId);
        final Post postToUpdate = getPost(postId);
        checkIfUserIsPostAuthor(requesterId, postToUpdate);

        DataFile currentFile = postToUpdate.getFile();
        if (file != null && !currentFile.getFileName().equals(file.getOriginalFilename())) {
            dataFileService.deleteFile(currentFile.getId());
            DataFile newFile = dataFileService.uploadFile(file, requesterId);
            postToUpdate.setFile(newFile);
        }
        if (content != null) {
            postToUpdate.setTitle(content);
        }

        postRepository.save(postToUpdate);
        long likes = postLikeRepository.findPostLikesCount(postId);
        postToUpdate.setNumberOfLikes(likes);
        log.info("Пост с id '{}' был обновлен пользователем с id '{}'.", postId, requesterId);
        return postToUpdate;
    }

    @Override
    @Transactional
    public void deletePost(long requesterId, long postId) {
        userService.findUserById(requesterId);
        final Post postToDelete = getPost(postId);
        checkIfUserIsPostAuthor(requesterId, postToDelete);
        DataFile file = postToDelete.getFile();
        dataFileService.deleteFile(file.getId());
        postRepository.deleteById(postId);
    }

    @Override
    public Post findPostById(long postId) {
        final Post post = getPostWithComments(postId);
        post.addView();
        postRepository.save(post);
        long likes = postLikeRepository.findPostLikesCount(postId);
        post.setNumberOfLikes(likes);
        log.info("Получение поста с id '{}'.", postId);
        return post;
    }

    @Override
    public List<Post> findPostsFromUser(long userId) {
        List<Post> posts = postRepository.findPostsByAuthorId(userId);
        log.info("Получение постов пользователя с id '{}'. Количество постов: '{}'.", userId, posts.size());
        return posts;
    }

    @Override
    @Transactional
    public long addLikeToPost(long requesterId, long postId) {
        User user = userService.findUserById(requesterId);
        Post post = getPost(postId);

        PostLikeId likeId = PostLikeId.of(post, user);
        Optional<PostLike> like = postLikeRepository.findById(likeId);

        if (like.isEmpty()) {
            postLikeRepository.save(new PostLike(likeId));
            log.info("Пользователь с id '{}' поставил лайк посту с id '{}'.", requesterId,
                    postId);
        } else {
            postLikeRepository.deleteById(likeId);
            log.info("Пользователь с id '{}' попытался повторно поставить лайк посту с id '{}'. Лайк удален.",
                    requesterId, postId);
        }

        long likesCount = postLikeRepository.findPostLikesCount(postId);
        post.setNumberOfLikes(likesCount);
        postRepository.save(post);
        log.info("Количество лайков поста с id '{}': '{}'.", postId, likesCount);
        return likesCount;
    }

    @Override
    public List<Post> getPostsFeed(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by("numberOfViews", "createdOn").descending());
        List<Post> feed = postRepository.getPostsFeed(pageable);
        log.info("Получен фид постов размером ");
        return feed;
    }

    @Override
    @Transactional
    public Comment addCommentToPost(long requesterId, long postId, Comment newComment) {
        User author = userService.findUserById(requesterId);
        Post post = getPost(postId);
        newComment.setAuthor(author);
        newComment.setPost(post);
        Comment savedComment = commentRepository.save(newComment);
        post.addComment(savedComment);
        postRepository.save(post);
        log.info("Пользователь с id '{} оставил комментарий на пост с id '{}'.", requesterId, postId);
        return savedComment;
    }

    @Override
    public Comment updateComment(long requesterId, long commentId, CommentRequest commentRequest) {
        userService.findUserById(requesterId);
        Comment comment = getCommentWithAuthor(commentId);
        checkIfUserIsCommentAuthor(requesterId, comment);
        comment.setText(commentRequest.getText());
        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь с id '{}' отредактировал комментарий с id '{}'.", requesterId, commentId);
        return savedComment;
    }

    @Override
    public void deleteComment(long requesterId, long commentId) {
        userService.findUserById(requesterId);
        Comment comment = getCommentWithAuthor(commentId);
        checkIfUserIsCommentAuthor(requesterId, comment);
        commentRepository.deleteById(commentId);
        log.info("Пользователь с id '{}' удалил комментарий с id '{}'.", requesterId, commentId);
    }

    private Post getPost(long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException("Пост с id '" + postId + "' не найден."));
    }

    private Post getPostWithComments(long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException("Пост с id '" + postId + "' не найден."));
    }

    private void checkIfUserIsPostAuthor(long userId, Post post) {
        if (userId != post.getAuthor().getId()) {
            throw new NotAuthorizedException("Пользователь с id '" + userId
                    + "' не имеет прав на редактирование поста с id '" + post.getId() + "'.");
        }
    }

    private void checkIfUserIsCommentAuthor(long requesterId, Comment comment) {
        if (comment.getAuthor().getId() != requesterId) {
            throw new NotAuthorizedException("Пользователь с id '" + requesterId
                    + "' не имеет прав на редактирование комментария с id '" + comment.getId() + "'.");
        }
    }

    private Comment getCommentWithAuthor(long commentId) {
        return commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id '" + commentId + "' не найден."));
    }
}
