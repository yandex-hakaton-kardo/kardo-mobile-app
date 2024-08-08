package ru.yandex.kardomoblieapp.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.dto.PostSearchFilter;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostLike;
import ru.yandex.kardomoblieapp.post.model.PostLikeId;
import ru.yandex.kardomoblieapp.post.model.PostSort;
import ru.yandex.kardomoblieapp.post.model.PostWithLike;
import ru.yandex.kardomoblieapp.post.repository.CommentRepository;
import ru.yandex.kardomoblieapp.post.repository.PostLikeRepository;
import ru.yandex.kardomoblieapp.post.repository.PostRepository;
import ru.yandex.kardomoblieapp.post.repository.PostSpecification;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Post createPost(String username, MultipartFile file, String content) {
        final User author = userService.findByUsername(username);
        final Post newPost = Post.builder()
                .author(author)
                .title(content)
                .build();

        DataFile uploadedFile = dataFileService.uploadFile(file, author.getId());
        newPost.setFile(uploadedFile);
        uploadedFile.setPost(newPost);
        Post savedPost = postRepository.save(newPost);
        log.info("Пользователь с id '{}' создал пост с id '{}'.", author.getId(), savedPost.getId());
        return savedPost;
    }

    @Override
    @Transactional
    public Post updatePost(String username, long postId, MultipartFile file, String content) {
        User user = userService.findByUsername(username);
        final Post postToUpdate = getPost(postId);

        DataFile currentFile = postToUpdate.getFile();
        if (file != null && !currentFile.getFileName().equals(file.getOriginalFilename())) {
            dataFileService.deleteFile(currentFile.getId());
            DataFile newFile = dataFileService.uploadFile(file, user.getId());
            postToUpdate.setFile(newFile);
        }
        if (content != null) {
            postToUpdate.setTitle(content);
        }

        postRepository.save(postToUpdate);
        long likes = postLikeRepository.findPostLikesCount(postId);
        postToUpdate.setLikes(likes);
        log.info("Пост с id '{}' был обновлен пользователем с id '{}'.", postId, user.getId());
        return postToUpdate;
    }

    @Override
    @Transactional
    public void deletePost(String username, long postId) {
        userService.findByUsername(username);
        final Post postToDelete = getPost(postId);
        DataFile file = postToDelete.getFile();
        dataFileService.deleteFile(file.getId());
        postRepository.deleteById(postId);
    }

    @Override
    @Transactional
    public PostWithLike findPostById(long postId, String username) {
        final Post post = getPostWithComments(postId);
        final User user = userService.findByUsername(username);
        post.addView();
        postRepository.save(post);
        long likes = postLikeRepository.findPostLikesCount(postId);
        post.setLikes(likes);
        final boolean isPostLikedByUser = postLikeRepository.existsById(PostLikeId.of(post, user));
        log.info("Получение поста с id '{}'.", postId);
        return new PostWithLike(post, isPostLikedByUser);
    }

    @Override
    public List<Post> findPostsFromUser(long userId) {
        List<Post> posts = postRepository.findPostsByAuthorId(userId);
        log.info("Получение постов пользователя с id '{}'. Количество постов: '{}'.", userId, posts.size());
        return posts;
    }

    @Override
    @Transactional
    public long addLikeToPost(String username, long postId) {
        User user = userService.findByUsername(username);
        Post post = getPost(postId);

        PostLikeId likeId = PostLikeId.of(post, user);
        Optional<PostLike> like = postLikeRepository.findById(likeId);

        if (like.isEmpty()) {
            postLikeRepository.save(new PostLike(likeId));
            log.info("Пользователь с id '{}' поставил лайк посту с id '{}'.", user.getId(),
                    postId);
        } else {
            postLikeRepository.deleteById(likeId);
            log.info("Пользователь с id '{}' попытался повторно поставить лайк посту с id '{}'. Лайк удален.",
                    user.getId(), postId);
        }

        long likesCount = postLikeRepository.findPostLikesCount(postId);
        post.setLikes(likesCount);
        postRepository.save(post);
        log.info("Количество лайков поста с id '{}': '{}'.", postId, likesCount);
        return likesCount;
    }

    @Override
    public List<Post> getPostsFeed(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("views", "createdOn").descending());
        List<Post> feed = postRepository.getPostsFeed(pageable);
        log.info("Получен фид постов размером ");
        return feed;
    }

    @Override
    @Transactional
    public Comment addCommentToPost(String username, long postId, Comment newComment) {
        User author = userService.findByUsername(username);
        Post post = getPost(postId);
        newComment.setAuthor(author);
        newComment.setPost(post);
        Comment savedComment = commentRepository.save(newComment);
        post.addComment(savedComment);
        postRepository.save(post);
        log.info("Пользователь с id '{} оставил комментарий на пост с id '{}'.", author.getId(), postId);
        return savedComment;
    }

    @Override
    public Comment updateComment(String username, long commentId, CommentRequest commentRequest) {
        Comment comment = getCommentWithAuthor(commentId);
        checkIfUserIsCommentAuthor(username, comment);
        comment.setText(commentRequest.getText());
        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь с id '{}' отредактировал комментарий с id '{}'.", username, commentId);
        return savedComment;
    }

    @Override
    public void deleteComment(String username, long commentId) {
        userService.findByUsername(username);
        Comment comment = getCommentWithAuthor(commentId);
        checkIfUserIsCommentAuthor(username, comment);
        commentRepository.deleteById(commentId);
        log.info("Пользователь с id '{}' удалил комментарий с id '{}'.", username, commentId);
    }

    @Override
    public List<Post> getRecommendations(String username, Integer page, Integer size, PostSort sort) {
        User user = userService.findByUsername(username);
        List<Long> friendsIds = new ArrayList<>(userService.getFriendsList(user.getId()).stream().map(User::getId).toList());
        friendsIds.add(user.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.name().toLowerCase()).descending());
        List<Post> recommendations = postRepository.findRecommendations(friendsIds, pageable);
        log.info("Получен список рекомендаций для пользователя с id '{}' длиной '{}'.", user.getId(), recommendations.size());
        return recommendations;
    }

    @Override
    public List<Post> searchPosts(PostSearchFilter searchFilter, Integer page, Integer size) {
        final Pageable pageable = PageRequest.of(page, size);
        final List<Specification<Post>> specifications = postSearchFilterToSpecifications(searchFilter);
        final Specification<Post> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        final List<Post> posts = postRepository.findAll(resultSpec, pageable).getContent();
        log.info("Получен список постов.");
        return posts;
    }

    private List<Specification<Post>> postSearchFilterToSpecifications(PostSearchFilter searchFilter) {
        final List<Specification<Post>> resultSpecification = new ArrayList<>();
        resultSpecification.add(PostSpecification.textInPostTitle(searchFilter.getTitle()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Post getPost(long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException("Пост с id '" + postId + "' не найден."));
    }

    private Post getPostWithComments(long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException("Пост с id '" + postId + "' не найден."));
    }

    private void checkIfUserIsCommentAuthor(String username, Comment comment) {
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new NotAuthorizedException("Пользователь с id '" + username
                    + "' не имеет прав на редактирование комментария с id '" + comment.getId() + "'.");
        }
    }

    private Comment getCommentWithAuthor(long commentId) {
        return commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id '" + commentId + "' не найден."));
    }
}
